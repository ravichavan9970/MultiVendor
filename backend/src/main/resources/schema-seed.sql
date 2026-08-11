-- Seed Initial Demo Data (BCrypt hashed password for 'password123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2)

-- Users (Seed Admin, Vendors, Customer by email check)
INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'admin@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'System Administrator', '+1 5550201', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.alex@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Alex Rivera', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.alex@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.sarah@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Sarah Chen', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.sarah@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.marcus@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Marcus Vance', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.marcus@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.elena@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Elena Rostova', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.elena@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.vikram@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Vikram Patel', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.vikram@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.chloe@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Chloe Dubois', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.chloe@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'vendor.david@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'David Kim', '+1 5550201', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'vendor.david@multivendor.com');

INSERT INTO users (email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at)
SELECT 'customer.john@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'John Doe', '+1 5550201', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer.john@gmail.com');

-- Vendor Profiles (linked by User Email)
INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'Rivera Tech & Algorithms Academy', 'TUTORING', 'Senior Software Engineer with 8+ years experience coaching Java, Data Structures, and System Design.', 'San Francisco, CA (Remote)', 85.00, 4.95, 42, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.alex@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'PixelCraft Digital Design Studio', 'FREELANCE', 'Award-winning UI/UX designer specializing in modern Web App interfaces, branding, and Figma design systems.', 'New York, NY (Remote)', 95.00, 4.90, 28, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.sarah@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'Apex Cloud & DevOps Engineering Lab', 'CONSULTING', 'AWS Certified Solutions Architect helping startups scale cloud infrastructure, Kubernetes, and CI/CD pipelines.', 'Austin, TX (Remote)', 120.00, 4.98, 54, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.marcus@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'Quantum AI & Machine Learning Studio', 'FREELANCE', 'AI Systems Architect specializing in Large Language Models (LLM), RAG architecture, PyTorch, and Vector DBs.', 'Seattle, WA (Remote)', 150.00, 4.92, 31, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.elena@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'CyberShield Security & Penetration Testing', 'CONSULTING', 'Certified Information Systems Security Professional (CISSP) performing web vulnerability audits and OAuth2 security.', 'London, UK (Remote)', 140.00, 4.96, 67, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.vikram@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'FullStack React & Next.js Design Lab', 'FREELANCE', 'Frontend Architect specializing in Next.js App Router, SSR performance tuning, and glassmorphism design systems.', 'Paris, France (Remote)', 90.00, 4.88, 39, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.chloe@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

INSERT INTO vendor_profiles (user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved, created_at)
SELECT u.id, 'Database Architecture & Performance Hub', 'TUTORING', 'Principal Database Administrator focusing on high-throughput MySQL indexing, PostgreSQL tuning, and NoSQL schemas.', 'Chicago, IL (Remote)', 105.00, 4.94, 48, TRUE, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'vendor.david@multivendor.com'
AND NOT EXISTS (SELECT 1 FROM vendor_profiles vp WHERE vp.user_id = u.id);

-- Service Items (Linked by Vendor Business Name)
INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, '1-on-1 Java & System Design Mock Interview', '60-minute intensive technical mock interview covering Spring Boot architecture, SQL locks, and live coding exercises with actionable feedback.', 'TUTORING', 60, 85.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Rivera Tech & Algorithms Academy'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = '1-on-1 Java & System Design Mock Interview');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Data Structures & Algorithms Deep-Dive', '60-minute problem-solving breakdown targeting LeetCode medium/hard patterns and code optimization.', 'TUTORING', 60, 75.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Rivera Tech & Algorithms Academy'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Data Structures & Algorithms Deep-Dive');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Complete UI/UX Audit & Figma Wireframing', 'Direct consultation to overhaul your website user interface, color system, and user flow.', 'FREELANCE', 60, 95.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'PixelCraft Digital Design Studio'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Complete UI/UX Audit & Figma Wireframing');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'AWS & Kubernetes Infrastructure Audit', 'Comprehensive 60-minute review of your cloud infrastructure, Terraform scripts, cost optimization, and CI/CD pipelines.', 'CONSULTING', 60, 120.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Apex Cloud & DevOps Engineering Lab'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'AWS & Kubernetes Infrastructure Audit');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Docker & Microservices Scaling Consultation', 'Hands-on session on containerizing legacy monoliths, service mesh setup, and zero-downtime deployment strategies.', 'CONSULTING', 60, 110.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Apex Cloud & DevOps Engineering Lab'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Docker & Microservices Scaling Consultation');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'LLM Integration & RAG Pipeline Workshop', 'Learn to build custom Retrieval-Augmented Generation (RAG) applications using LangChain, Vector Databases (Pinecone/Milvus), and OpenAI APIs.', 'FREELANCE', 90, 150.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Quantum AI & Machine Learning Studio'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'LLM Integration & RAG Pipeline Workshop');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Python PyTorch & Deep Learning Model Audit', 'Model performance profiling, hyperparameter tuning, and PyTorch deployment to GPU edge instances.', 'FREELANCE', 60, 130.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Quantum AI & Machine Learning Studio'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Python PyTorch & Deep Learning Model Audit');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Web Application Vulnerability & OWASP Audit', 'Deep security assessment covering SQL injection, XSS, CSRF, JWT validation flaws, and rate-limiting security headers.', 'CONSULTING', 60, 140.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'CyberShield Security & Penetration Testing'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Web Application Vulnerability & OWASP Audit');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'API Authentication & OAuth2.0 Security Coaching', 'Step-by-step guidance on implementing OAuth2, PKCE, Single Sign-On (SSO), and JWT refresh token rotation.', 'CONSULTING', 45, 95.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'CyberShield Security & Penetration Testing'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'API Authentication & OAuth2.0 Security Coaching');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'React & Next.js Performance & SSR Optimization', 'Audit your Next.js App Router, React Server Components (RSC), bundle size reduction, and Core Web Vitals score tuning.', 'FREELANCE', 60, 90.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'FullStack React & Next.js Design Lab'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'React & Next.js Performance & SSR Optimization');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Modern Glassmorphism & Micro-Animation Mastery', 'Learn how to craft ultra-premium Dark Mode UIs with modern CSS variables, glassmorphism, dynamic gradients, and micro-animations.', 'FREELANCE', 60, 80.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'FullStack React & Next.js Design Lab'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Modern Glassmorphism & Micro-Animation Mastery');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'MySQL & PostgreSQL Indexing & Query Tuning', 'Master EXPLAIN ANALYZE, B-Tree vs Hash indexes, deadlock prevention, and multi-million row SQL query optimization.', 'TUTORING', 60, 100.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Database Architecture & Performance Hub'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'MySQL & PostgreSQL Indexing & Query Tuning');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'NoSQL vs Relational Schema Architecture Review', 'Architectural deep-dive to pick the right DB engine (MySQL, MongoDB, Redis, Cassandra) for your high-throughput app.', 'TUTORING', 60, 105.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Database Architecture & Performance Hub'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'NoSQL vs Relational Schema Architecture Review');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Design System & Mobile Component Library Audit', 'Transform scattered UI components into a clean, reusable design token system for web and mobile platforms.', 'FREELANCE', 60, 110.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'PixelCraft Digital Design Studio'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Design System & Mobile Component Library Audit');

INSERT INTO service_items (vendor_id, title, description, category, duration_minutes, price, is_active, created_at)
SELECT vp.id, 'Code Review & Spring Boot Security Hardening', 'Line-by-line code review of your Java backend focusing on Spring Security matchers, JWT secret safety, and transaction boundaries.', 'TUTORING', 60, 95.00, TRUE, CURRENT_TIMESTAMP
FROM vendor_profiles vp WHERE vp.business_name = 'Rivera Tech & Algorithms Academy'
AND NOT EXISTS (SELECT 1 FROM service_items si WHERE si.title = 'Code Review & Spring Boot Security Hardening');

-- Availability Slots for all service items (MySQL compatible DATE_ADD)
INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = '1-on-1 Java & System Design Mock Interview'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);

INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = 'AWS & Kubernetes Infrastructure Audit'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);

INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = 'LLM Integration & RAG Pipeline Workshop'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);

INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = 'Web Application Vulnerability & OWASP Audit'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);

INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = 'React & Next.js Performance & SSR Optimization'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);

INSERT INTO availability_slots (service_id, start_time, end_time, status, version, created_at)
SELECT si.id, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'AVAILABLE', 0, CURRENT_TIMESTAMP
FROM service_items si WHERE si.title = 'MySQL & PostgreSQL Indexing & Query Tuning'
AND NOT EXISTS (SELECT 1 FROM availability_slots WHERE service_id = si.id);
