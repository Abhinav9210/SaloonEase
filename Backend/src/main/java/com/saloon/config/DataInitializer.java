package com.saloon.config;

import com.saloon.entity.*;
import com.saloon.entity.enums.Role;
import com.saloon.entity.enums.SalonStatus;
import com.saloon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

/**
 * DataInitializer — seeds demo accounts and sample data on first startup.
 *
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         DEMO LOGIN CREDENTIALS  (password: Password@123)    ║
 * ╠═══════════════╦══════════════════════════════════════════════╣
 * ║ Role          ║ Email                                        ║
 * ╠═══════════════╬══════════════════════════════════════════════╣
 * ║ ADMIN         ║ admin@salonease.com                         ║
 * ║ OWNER         ║ owner@salonease.com                         ║
 * ║ BARBER        ║ barber@salonease.com                        ║
 * ║ CUSTOMER      ║ customer@salonease.com                      ║
 * ╚═══════════════╩══════════════════════════════════════════════╝
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository    userRepository;
    private final SalonRepository   salonRepository;
    private final BarberRepository  barberRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder   passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already has data — skipping seed.");
                return;
            }

            final String PWD = passwordEncoder.encode("Password@123");

            // ══ 1. Users ═════════════════════════════════════════════════════
            User admin = save(user("Super Admin",    "admin@salonease.com",    PWD, Role.ADMIN));
            User owner1 = save(user("Rajesh Kumar",  "owner@salonease.com",    PWD, Role.OWNER));
            User owner2 = save(user("Priya Sharma",  "owner2@salonease.com",   PWD, Role.OWNER));
            User barberUser1 = save(user("Mohammed Ali",   "barber@salonease.com",  PWD, Role.BARBER));
            User barberUser2 = save(user("Surinder Singh", "barber2@salonease.com", PWD, Role.BARBER));
            User barberUser3 = save(user("Deepak Mehta",   "barber3@salonease.com", PWD, Role.BARBER));
            User barberUser4 = save(user("Arjun Nair",     "barber4@salonease.com", PWD, Role.BARBER));
            User customer    = save(user("Abhinav Monga",  "customer@salonease.com",PWD, Role.CUSTOMER));
            // extra demo customers
            save(user("Sneha Patel",  "customer2@salonease.com", PWD, Role.CUSTOMER));
            save(user("Vikram Reddy", "customer3@salonease.com", PWD, Role.CUSTOMER));

            log.info("✓ Users seeded: {}", userRepository.count());

            // ══ 2. Salons ════════════════════════════════════════════════════
            Salon s1 = salonRepository.save(Salon.builder()
                .owner(owner1).name("The Royal Cuts")
                .description("Premium grooming studio with expert barbers specializing in modern and classic cuts. Walk-in and appointment welcome.")
                .address("12, MG Road").city("Bangalore").state("Karnataka").pincode("560001")
                .phone("+91 80 1234 5678").openTime("09:00").closeTime("21:00")
                .minimumBookingFee(100.0).rating(4.7).totalReviews(128)
                .status(SalonStatus.APPROVED)
                .workingDays(Arrays.asList("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"))
                .build());

            Salon s2 = salonRepository.save(Salon.builder()
                .owner(owner1).name("Classic Blades")
                .description("Old-school barbershop vibes with modern precision. Straight razor shaves are our speciality.")
                .address("45, Indiranagar").city("Bangalore").state("Karnataka").pincode("560038")
                .phone("+91 80 9876 5432").openTime("10:00").closeTime("20:00")
                .minimumBookingFee(150.0).rating(4.5).totalReviews(87)
                .status(SalonStatus.APPROVED)
                .workingDays(Arrays.asList("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"))
                .build());

            Salon s3 = salonRepository.save(Salon.builder()
                .owner(owner2).name("Luxe Grooming Lounge")
                .description("Luxury grooming experience with premium products and a relaxing ambience. Bandra's finest.")
                .address("7, Bandra West").city("Mumbai").state("Maharashtra").pincode("400050")
                .phone("+91 22 3456 7890").openTime("09:30").closeTime("20:30")
                .minimumBookingFee(200.0).rating(4.9).totalReviews(210)
                .status(SalonStatus.APPROVED)
                .workingDays(Arrays.asList("TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"))
                .build());

            Salon s4 = salonRepository.save(Salon.builder()
                .owner(owner2).name("Urban Fade Studio")
                .description("Creative cuts for the modern man. Trending styles and custom fades.")
                .address("88, Koregaon Park").city("Pune").state("Maharashtra").pincode("411001")
                .phone("+91 20 5678 1234").openTime("10:00").closeTime("19:00")
                .minimumBookingFee(120.0).rating(4.3).totalReviews(62)
                .status(SalonStatus.APPROVED)
                .workingDays(Arrays.asList("MONDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"))
                .build());

            // Pending salon — visible in admin approval queue
            salonRepository.save(Salon.builder()
                .owner(owner2).name("New Style Studio — Pending")
                .description("Upcoming hip barbershop in FC Road Pune. Awaiting approval.")
                .address("23, FC Road").city("Pune").state("Maharashtra").pincode("411004")
                .openTime("10:00").closeTime("19:00").minimumBookingFee(80.0)
                .status(SalonStatus.PENDING)
                .workingDays(Arrays.asList("MONDAY","WEDNESDAY","FRIDAY","SATURDAY"))
                .build());

            log.info("✓ Salons seeded: {}", salonRepository.count());

            // ══ 3. Barbers (linked to their User accounts) ═══════════════════
            barberRepository.save(Barber.builder()
                .salon(s1).user(barberUser1)
                .bio("10 years of fades, tapers and modern cuts. Instagram-worthy styles guaranteed.")
                .experienceYears(10).rating(4.8).totalReviews(95).isAvailable(true)
                .specializations(Arrays.asList("Fade", "Modern Cut", "Beard Styling"))
                .build());

            barberRepository.save(Barber.builder()
                .salon(s1).user(barberUser2)
                .bio("Classic cuts and straight-razor shaves. Traditional barbering at its finest.")
                .experienceYears(7).rating(4.6).totalReviews(72).isAvailable(true)
                .specializations(Arrays.asList("Straight Razor", "Classic Cut", "Hot Towel Shave"))
                .build());

            barberRepository.save(Barber.builder()
                .salon(s2).user(barberUser3)
                .bio("Creative stylist with a passion for textured cuts and bold colors.")
                .experienceYears(4).rating(4.4).totalReviews(43).isAvailable(true)
                .specializations(Arrays.asList("Textured Cut", "Color", "Pompadour"))
                .build());

            barberRepository.save(Barber.builder()
                .salon(s3).user(barberUser4)
                .bio("Luxury grooming specialist. 12 years building Mumbai's finest grooming experience.")
                .experienceYears(12).rating(4.9).totalReviews(156).isAvailable(true)
                .specializations(Arrays.asList("Luxury Grooming", "Skin Care", "Beard Sculpting"))
                .build());

            log.info("✓ Barbers seeded: {}", barberRepository.count());

            // ══ 4. Services ══════════════════════════════════════════════════
            // Salon 1 — The Royal Cuts
            svc(s1, "Haircut",         "Classic haircut with wash and blow-dry",               200, 30);
            svc(s1, "Skin Fade",       "Zero-to-skin gradient fade cut",                       350, 45);
            svc(s1, "Beard Trim",      "Shape and precision trim with razor edging",            150, 20);
            svc(s1, "Hair + Beard",    "Full grooming combo — cut and beard shape",             450, 60);
            svc(s1, "Hot Towel Shave", "Traditional hot-towel straight razor shave",           300, 40);
            svc(s1, "Kids Cut",        "Gentle cut for children under 12",                     150, 25);

            // Salon 2 — Classic Blades
            svc(s2, "Classic Cut",     "No-frills professional cut and style",                  180, 30);
            svc(s2, "Straight Shave",  "Old-school straight razor shave",                       350, 45);
            svc(s2, "Beard Design",    "Custom beard shape and razor design",                   200, 30);
            svc(s2, "Color Treatment", "Single process hair color",                              800, 90);
            svc(s2, "Perm",            "Soft wave or tight curl perm treatment",               1200, 120);

            // Salon 3 — Luxe Grooming Lounge
            svc(s3, "Luxury Haircut",  "Premium cut with head massage and hot towel",           500, 60);
            svc(s3, "Royal Shave",     "Multi-step luxury hot-towel shave experience",          600, 60);
            svc(s3, "Beard Sculpting", "Artisan beard shaping by our master barber",           400, 45);
            svc(s3, "Facial",          "Deep-cleansing and relaxing skin facial",               800, 75);
            svc(s3, "Full Package",    "Luxury Cut + Royal Shave + Facial — the complete works",1500, 120);

            // Salon 4 — Urban Fade Studio
            svc(s4, "Urban Cut",       "Modern tailored cut with textured finish",              250, 35);
            svc(s4, "Taper Fade",      "Clean gradient taper fade",                             300, 40);
            svc(s4, "Beard & Fade",    "Taper fade + beard edging combo",                       450, 55);
            svc(s4, "Design Cut",      "Custom design or pattern shaved into hair",             500, 60);

            log.info("✓ Services seeded: {}", serviceRepository.count());

            log.info("==========================================================");
            log.info("  DEMO LOGIN CREDENTIALS  (password: Password@123)");
            log.info("  ADMIN    → admin@salonease.com    → /dashboard/admin");
            log.info("  OWNER    → owner@salonease.com    → /dashboard/owner");
            log.info("  BARBER   → barber@salonease.com   → /dashboard/barber");
            log.info("  CUSTOMER → customer@salonease.com → /dashboard/customer");
            log.info("==========================================================");
        };
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User user(String name, String email, String pwd, Role role) {
        return User.builder().name(name).email(email).password(pwd).role(role).build();
    }

    private User save(User u) {
        return userRepository.save(u);
    }

    private void svc(Salon salon, String name, String description, int price, int duration) {
        serviceRepository.save(Service.builder()
            .salon(salon).name(name).description(description)
            .price((double) price).durationMinutes(duration).isActive(true)
            .build());
    }
}
