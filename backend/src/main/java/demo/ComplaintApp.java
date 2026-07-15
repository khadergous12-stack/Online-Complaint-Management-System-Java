package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.security.SecureRandom;

// Single file Spring Boot application
// Online Complaint Management System - REST API
@SpringBootApplication
@RestController
@RequestMapping("/api")
@Validated
public class ComplaintApp {

    public static void main(String[] args) {
        SpringApplication.run(ComplaintApp.class, args);
    }

    /**
     * Allows the standalone dashboard/index.html (opened from disk or
     * served on a different port, e.g. via VS Code Live Server) to call
     * this API during local development.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

    // Simple in memory stores
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Complaint> complaints = new ConcurrentHashMap<>();
    private final Map<Long, List<Comment>> comments = new ConcurrentHashMap<>();

    private final AtomicLong userSeq = new AtomicLong(100);
    private final AtomicLong compSeq = new AtomicLong(1000);
    private final AtomicLong commentSeq = new AtomicLong(5000);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static final SecureRandom RNG = new SecureRandom();

    // Seed demo users + a couple of demo tickets so the dashboard has data on first run
    @PostConstruct
    public void init() {
        User admin = createUser("Admin", "admin@example.com", "admin", Role.ADMIN);
        User student = createUser("Student", "user@example.com", "user", Role.USER);

        Complaint c1 = new Complaint(compSeq.incrementAndGet(), student.id(), "Wi-Fi down in Block C",
                "Hostel Wi-Fi has been unreachable since this morning, affecting the whole floor.",
                Category.IT, Priority.High);
        complaints.put(c1.id, c1);
        comments.put(c1.id, new ArrayList<>());

        Complaint c2 = new Complaint(compSeq.incrementAndGet(), student.id(), "Broken chair in Lab 2",
                "One of the lab chairs has a broken leg and is unsafe to sit on.",
                Category.Facility, Priority.Low);
        c2.status = Status.IN_PROGRESS;
        c2.assignee = admin.id();
        complaints.put(c2.id, c2);
        comments.put(c2.id, new ArrayList<>());
    }

    private User createUser(String name, String email, String pass, Role role) {
        long id = userSeq.incrementAndGet();
        User u = new User(id, name, email, pass, role);
        users.put(id, u);
        return u;
    }

    // Models
    enum Role { USER, ADMIN }
    enum Priority { Low, Medium, High }
    enum Category { Facility, IT, Academic, Finance, Other }
    enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    record User(long id, String name, String email, String password, Role role) {}
    record Session(String token, long userId, Role role, Instant createdAt) {}

    static class Complaint {
        final long id;
        final long userId;
        String title;
        String description;
        Category category;
        Priority priority;
        Status status;
        Instant createdAt;
        Instant updatedAt;
        Long assignee; // user id of admin

        Complaint(long id, long userId, String title, String description, Category category, Priority priority) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = priority;
            this.status = Status.OPEN;
            this.createdAt = Instant.now();
            this.updatedAt = this.createdAt;
        }
    }

    static class Comment {
        final long id;
        final long complaintId;
        final long authorId;
        final String text;
        final Instant createdAt;
        Comment(long id, long complaintId, long authorId, String text) {
            this.id = id;
            this.complaintId = complaintId;
            this.authorId = authorId;
            this.text = text;
            this.createdAt = Instant.now();
        }
    }

    // DTOs
    record RegisterReq(
            @NotBlank String name,
            @Email String email,
            @Size(min = 4) String password
    ) {}
    record LoginReq(@Email String email, @NotBlank String password) {}
    record LoginRes(String token, long userId, String name, Role role) {}

    record CreateComplaintReq(
            @Size(min = 3) String title,
            @Size(min = 10) String description,
            @NotNull Category category,
            @NotNull Priority priority
    ) {}
    record ComplaintRes(
            long id, long userId, String title, String description,
            Category category, Priority priority, Status status,
            Instant createdAt, Instant updatedAt, Long assignee,
            List<CommentRes> comments
    ) {}
    record CommentReq(@Size(min = 2) String text) {}
    record CommentRes(long id, long authorId, String authorName, String text, Instant createdAt) {}

    record AssignReq(Long assigneeId) {}
    record StatusReq(@NotNull Status status) {}

    // Auth helpers
    private Optional<Session> resolveSession(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.ofNullable(sessions.get(token));
    }
    private String newToken() {
        byte[] b = new byte[24];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private void checkAdmin(Session s) {
        if (s.role() != Role.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
    }

    // Endpoints

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req) {
        var violations = validator.validate(req);
        if (!violations.isEmpty()) return badRequest(violations);
        boolean exists = users.values().stream().anyMatch(u -> u.email().equalsIgnoreCase(req.email()));
        if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = createUser(req.name(), req.email(), req.password(), Role.USER);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", u.id(), "email", u.email()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginRes> login(@RequestBody LoginReq req) {
        User u = users.values().stream()
                .filter(x -> x.email().equalsIgnoreCase(req.email()) && x.password().equals(req.password()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        String token = newToken();
        sessions.put(token, new Session(token, u.id(), u.role(), Instant.now()));
        return ResponseEntity.ok(new LoginRes(token, u.id(), u.name(), u.role()));
    }

    @GetMapping("/complaints/mine")
    public List<ComplaintRes> myComplaints(@RequestHeader("X-Auth") String token) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return complaints.values().stream()
                .filter(c -> c.userId == s.userId())
                .sorted(Comparator.comparing((Complaint c) -> c.createdAt).reversed())
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @PostMapping("/complaints")
    public ComplaintRes createComplaint(@RequestHeader("X-Auth") String token,
                                        @RequestBody CreateComplaintReq req) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var violations = validator.validate(req);
        if (!violations.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, joinViolations(violations));
        long id = compSeq.incrementAndGet();
        Complaint c = new Complaint(id, s.userId(), req.title(), req.description(), req.category(), req.priority());
        complaints.put(id, c);
        comments.put(id, new ArrayList<>());
        return toRes(c);
    }

    @GetMapping("/complaints/{id}")
    public ComplaintRes getComplaint(@RequestHeader("X-Auth") String token, @PathVariable long id) {
        resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Complaint c = complaints.get(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        return toRes(c);
    }

    @PostMapping("/complaints/{id}/comments")
    public ComplaintRes addComment(@RequestHeader("X-Auth") String token,
                                   @PathVariable long id,
                                   @RequestBody CommentReq req) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var violations = validator.validate(req);
        if (!violations.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, joinViolations(violations));
        Complaint c = complaints.get(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        long cid = commentSeq.incrementAndGet();
        Comment cm = new Comment(cid, id, s.userId(), req.text());
        comments.get(id).add(cm);
        c.updatedAt = Instant.now();
        return toRes(c);
    }

    @PatchMapping("/complaints/{id}/assign")
    public ComplaintRes assign(@RequestHeader("X-Auth") String token,
                               @PathVariable long id,
                               @RequestBody(required = false) AssignReq req) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        checkAdmin(s);
        Complaint c = complaints.get(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");

        // If no assignee is supplied, assign to the requesting admin ("assign to me")
        long assigneeId = (req != null && req.assigneeId() != null) ? req.assigneeId() : s.userId();
        User target = users.get(assigneeId);
        if (target == null || target.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must be an admin");
        }
        c.assignee = target.id();
        c.status = Status.IN_PROGRESS;
        c.updatedAt = Instant.now();
        return toRes(c);
    }

    @PatchMapping("/complaints/{id}/status")
    public ComplaintRes changeStatus(@RequestHeader("X-Auth") String token,
                                     @PathVariable long id,
                                     @RequestBody StatusReq req) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Complaint c = complaints.get(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");

        // Admin may move to any status except back to OPEN
        if (s.role() == Role.ADMIN) {
            if (req.status() == Status.OPEN) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move back to open");
            c.status = req.status();
        } else {
            // User may only close a resolved complaint of their own
            if (c.userId != s.userId()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your complaint");
            if (req.status() != Status.CLOSED || c.status != Status.RESOLVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You may close only a resolved complaint");
            }
            c.status = Status.CLOSED;
        }
        c.updatedAt = Instant.now();
        return toRes(c);
    }

    @GetMapping("/complaints/search")
    public List<ComplaintRes> search(@RequestHeader("X-Auth") String token,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String from,
                                     @RequestParam(required = false) String to,
                                     @RequestParam(required = false) String q) {
        Session s = resolveSession(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        checkAdmin(s);
        return complaints.values().stream()
                .filter(c -> status == null || status.isBlank() || c.status.name().equalsIgnoreCase(status))
                .filter(c -> category == null || category.isBlank() || c.category.name().equalsIgnoreCase(category))
                .filter(c -> q == null || q.isBlank()
                        || c.title.toLowerCase().contains(q.toLowerCase())
                        || c.description.toLowerCase().contains(q.toLowerCase()))
                .sorted(Comparator.comparing((Complaint c) -> c.createdAt).reversed())
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    // Helpers
    private ComplaintRes toRes(Complaint c) {
        List<CommentRes> list = comments.getOrDefault(c.id, List.of()).stream()
                .sorted(Comparator.comparing(cm -> cm.createdAt))
                .map(cm -> new CommentRes(cm.id, cm.authorId, users.get(cm.authorId).name(), cm.text, cm.createdAt))
                .collect(Collectors.toList());
        return new ComplaintRes(
                c.id, c.userId, c.title, c.description, c.category, c.priority, c.status,
                c.createdAt, c.updatedAt, c.assignee, list
        );
    }

    private ResponseEntity<Map<String, Object>> badRequest(Set<? extends ConstraintViolation<?>> violations) {
        String msg = joinViolations(violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    private String joinViolations(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(v -> v.getPropertyPath() + " " + v.getMessage()).collect(Collectors.joining("; "));
    }
}
