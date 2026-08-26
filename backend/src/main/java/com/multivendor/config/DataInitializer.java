package com.multivendor.config;

import com.multivendor.model.*;
import com.multivendor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           VendorProfileRepository vendorProfileRepository,
                           ServiceItemRepository serviceItemRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("🚀 Database is empty. Initializing seed data and demo accounts...");
            initSeedData();
        } else {
            log.info("💾 Existing database detected with {} users. Permanent data storage preserved.", userRepository.count());
            ensureSlotsAvailableForActiveServices();
        }
    }

    private void initSeedData() {
        String encodedPassword = passwordEncoder.encode("password123");

        // 1. Admin
        User admin = new User("admin@multivendor.com", encodedPassword, "System Administrator", "+1 5550201", Role.ADMIN);
        userRepository.save(admin);

        // 2. Customer
        User customer = new User("customer.john@gmail.com", encodedPassword, "John Doe", "+1 5550209", Role.CUSTOMER);
        userRepository.save(customer);

        // 3. Vendors & Services
        createVendorWithServices(
                "vendor.alex@multivendor.com", encodedPassword, "Alex Rivera", "+1 5550202",
                "Rivera Tech & Algorithms Academy", "TUTORING",
                "Senior Software Engineer with 8+ years experience coaching Java, Data Structures, and System Design.",
                "San Francisco, CA (Remote)", BigDecimal.valueOf(85.00),
                List.of(
                        new ServiceSeed("1-on-1 Java & System Design Mock Interview",
                                "60-minute intensive technical mock interview covering Spring Boot architecture, SQL locks, and live coding exercises with actionable feedback.",
                                "TUTORING", 60, BigDecimal.valueOf(85.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Data Structures & Algorithms Deep-Dive",
                                "60-minute problem-solving breakdown targeting LeetCode medium/hard patterns and code optimization.",
                                "TUTORING", 60, BigDecimal.valueOf(75.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Code Review & Spring Boot Security Hardening",
                                "Line-by-line code review of your Java backend focusing on Spring Security matchers, JWT secret safety, and transaction boundaries.",
                                "TUTORING", 60, BigDecimal.valueOf(95.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.sarah@multivendor.com", encodedPassword, "Sarah Chen", "+1 5550203",
                "PixelCraft Digital Design Studio", "FREELANCE",
                "Award-winning UI/UX designer specializing in modern Web App interfaces, branding, and Figma design systems.",
                "New York, NY (Remote)", BigDecimal.valueOf(95.00),
                List.of(
                        new ServiceSeed("Complete UI/UX Audit & Figma Wireframing",
                                "Direct consultation to overhaul your website user interface, color system, and user flow.",
                                "FREELANCE", 60, BigDecimal.valueOf(95.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Design System & Mobile Component Library Audit",
                                "Transform scattered UI components into a clean, reusable design token system for web and mobile platforms.",
                                "FREELANCE", 60, BigDecimal.valueOf(110.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.marcus@multivendor.com", encodedPassword, "Marcus Vance", "+1 5550204",
                "Apex Cloud & DevOps Engineering Lab", "CONSULTING",
                "AWS Certified Solutions Architect helping startups scale cloud infrastructure, Kubernetes, and CI/CD pipelines.",
                "Austin, TX (Remote)", BigDecimal.valueOf(120.00),
                List.of(
                        new ServiceSeed("AWS & Kubernetes Infrastructure Audit",
                                "Comprehensive 60-minute review of your cloud infrastructure, Terraform scripts, cost optimization, and CI/CD pipelines.",
                                "CONSULTING", 60, BigDecimal.valueOf(120.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Docker & Microservices Scaling Consultation",
                                "Hands-on session on containerizing legacy monoliths, service mesh setup, and zero-downtime deployment strategies.",
                                "CONSULTING", 60, BigDecimal.valueOf(110.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.elena@multivendor.com", encodedPassword, "Elena Rostova", "+1 5550205",
                "Quantum AI & Machine Learning Studio", "FREELANCE",
                "AI Systems Architect specializing in Large Language Models (LLM), RAG architecture, PyTorch, and Vector DBs.",
                "Seattle, WA (Remote)", BigDecimal.valueOf(150.00),
                List.of(
                        new ServiceSeed("LLM Integration & RAG Pipeline Workshop",
                                "Learn to build custom Retrieval-Augmented Generation (RAG) applications using LangChain, Vector Databases, and OpenAI APIs.",
                                "FREELANCE", 90, BigDecimal.valueOf(150.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Python PyTorch & Deep Learning Model Audit",
                                "Model performance profiling, hyperparameter tuning, and PyTorch deployment to GPU edge instances.",
                                "FREELANCE", 60, BigDecimal.valueOf(130.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.vikram@multivendor.com", encodedPassword, "Vikram Patel", "+1 5550206",
                "CyberShield Security & Penetration Testing", "CONSULTING",
                "Certified Information Systems Security Professional (CISSP) performing web vulnerability audits and OAuth2 security.",
                "London, UK (Remote)", BigDecimal.valueOf(140.00),
                List.of(
                        new ServiceSeed("Web Application Vulnerability & OWASP Audit",
                                "Deep security assessment covering SQL injection, XSS, CSRF, JWT validation flaws, and rate-limiting security headers.",
                                "CONSULTING", 60, BigDecimal.valueOf(140.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("API Authentication & OAuth2.0 Security Coaching",
                                "Step-by-step guidance on implementing OAuth2, PKCE, Single Sign-On (SSO), and JWT refresh token rotation.",
                                "CONSULTING", 45, BigDecimal.valueOf(95.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.chloe@multivendor.com", encodedPassword, "Chloe Dubois", "+1 5550207",
                "FullStack React & Next.js Design Lab", "FREELANCE",
                "Frontend Architect specializing in Next.js App Router, SSR performance tuning, and glassmorphism design systems.",
                "Paris, France (Remote)", BigDecimal.valueOf(90.00),
                List.of(
                        new ServiceSeed("React & Next.js Performance & SSR Optimization",
                                "Audit your Next.js App Router, React Server Components (RSC), bundle size reduction, and Core Web Vitals score tuning.",
                                "FREELANCE", 60, BigDecimal.valueOf(90.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("Modern Glassmorphism & Micro-Animation Mastery",
                                "Learn how to craft ultra-premium Dark Mode UIs with modern CSS variables, glassmorphism, dynamic gradients, and micro-animations.",
                                "FREELANCE", 60, BigDecimal.valueOf(80.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        createVendorWithServices(
                "vendor.david@multivendor.com", encodedPassword, "David Kim", "+1 5550208",
                "Database Architecture & Performance Hub", "TUTORING",
                "Principal Database Administrator focusing on high-throughput MySQL indexing, PostgreSQL tuning, and NoSQL schemas.",
                "Chicago, IL (Remote)", BigDecimal.valueOf(105.00),
                List.of(
                        new ServiceSeed("MySQL & PostgreSQL Indexing & Query Tuning",
                                "Master EXPLAIN ANALYZE, B-Tree vs Hash indexes, deadlock prevention, and multi-million row SQL query optimization.",
                                "TUTORING", 60, BigDecimal.valueOf(100.00), "https://meet.google.com/dae-zpiu-oau"),
                        new ServiceSeed("NoSQL vs Relational Schema Architecture Review",
                                "Architectural deep-dive to pick the right DB engine (MySQL, MongoDB, Redis, Cassandra) for your high-throughput app.",
                                "TUTORING", 60, BigDecimal.valueOf(105.00), "https://meet.google.com/dae-zpiu-oau")
                )
        );

        log.info("✅ Database initialization complete! Demo users, services, and dynamic availability slots created.");
    }

    private void createVendorWithServices(String email, String password, String fullName, String phone,
                                         String businessName, String category, String bio, String location,
                                         BigDecimal rate, List<ServiceSeed> services) {
        User user = new User(email, password, fullName, phone, Role.VENDOR);
        User savedUser = userRepository.save(user);

        VendorProfile profile = new VendorProfile(savedUser, businessName, category, bio, location, rate);
        profile.setRating(BigDecimal.valueOf(4.90));
        profile.setTotalReviews(35);
        profile.setIsApproved(true);
        VendorProfile savedProfile = vendorProfileRepository.save(profile);

        for (ServiceSeed s : services) {
            ServiceItem item = new ServiceItem(savedProfile, s.title, s.description, s.category, s.durationMinutes, s.price, s.meetingLink);
            ServiceItem savedItem = serviceItemRepository.save(item);

            // Generate slots for upcoming 5 days (10:00, 14:00, 16:00, 18:00 each day)
            generateSlotsForService(savedItem);
        }
    }

    private void generateSlotsForService(ServiceItem service) {
        LocalDateTime now = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        int[] hours = {10, 14, 16, 18};

        for (int day = 0; day < 5; day++) {
            LocalDateTime dayBase = now.plusDays(day);
            for (int hour : hours) {
                LocalDateTime startTime = dayBase.withHour(hour);
                LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());
                AvailabilitySlot slot = new AvailabilitySlot(service, startTime, endTime);
                slot.setStatus(SlotStatus.AVAILABLE);
                availabilitySlotRepository.save(slot);
            }
        }
    }

    private void ensureSlotsAvailableForActiveServices() {
        List<ServiceItem> activeServices = serviceItemRepository.findAll().stream().filter(ServiceItem::getIsActive).toList();
        for (ServiceItem s : activeServices) {
            List<AvailabilitySlot> existing = availabilitySlotRepository.findByServiceId(s.getId());
            long availableCount = existing.stream().filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE).count();
            if (availableCount < 3) {
                generateSlotsForService(s);
                log.info("⚡ Generated fresh availability slots for service: {}", s.getTitle());
            }
        }
    }

    public void resetAllData() {
        log.info("🧹 Initiating FULL Database Reset from Admin Panel...");
        bookingRepository.deleteAll();
        availabilitySlotRepository.deleteAll();
        serviceItemRepository.deleteAll();
        vendorProfileRepository.deleteAll();
        userRepository.deleteAll();
        initSeedData();
        log.info("✨ Full Reset Complete! Database reseeded with default demo data.");
    }

    private static class ServiceSeed {
        String title;
        String description;
        String category;
        int durationMinutes;
        BigDecimal price;
        String meetingLink;

        ServiceSeed(String title, String description, String category, int durationMinutes, BigDecimal price, String meetingLink) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.meetingLink = meetingLink;
        }
    }
}
