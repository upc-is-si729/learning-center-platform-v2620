workspace "ACME Learning Ecosystem Solution - C4 Model" "System-wide software architecture for the Learning Ecosystem Solution." {

    model {
        // People
        learner = person "Learner" "Consumes learning content and tracks progress."
        instructor = person "Instructor" "Creates and manages course content."
        admin = person "Platform Administrator" "Configures products, users, and catalog governance."
        support = person "Support Analyst" "Investigates incidents and supports operations."

        // External systems outside the whole solution boundary
        identityProvider = softwareSystem "Identity Provider" "External authentication provider (OIDC/OAuth2)." {
            tags "External System"
        }

        paymentGateway = softwareSystem "Payment Gateway" "Processes payments for paid offerings." {
            tags "External System"
        }

        emailProvider = softwareSystem "Email Provider" "Delivers transactional and notification emails." {
            tags "External System"
        }

        videoPlatform = softwareSystem "Video Platform" "Streams and stores course media assets." {
            tags "External System"
        }

        // Whole system scope (context level)
        solution = softwareSystem "Learning Ecosystem Solution" "Multi-product learning platform." {

            // Product containers inside the same solution
            webApp = container "Learning Web Application" "Delivers static content, including the Angular single-page application, to the user's web browser." "HTML, CSS, JavaScript" {
                tags "Directory"
            }

            singlePageApp = container "Learning Single-Page Application" "Provides the learning experience in the user's web browser for learners, instructors, and administrators." "Angular and TypeScript" {
                tags "Web Browser"
            }

            iosApp = container "Learning iOS Application" "Native iOS mobile application for learner-first experiences." "Swift and SwiftUI" {
                tags "Mobile App"
            }

            androidApp = container "Learning Android Application" "Native Android mobile application for learner-first experiences." "Kotlin and Jetpack Compose" {
                tags "Mobile App"
            }


            // This repository
            learningApi = container "Learning Center REST API" "Provides backend REST APIs for IAM, profiles, courses, students, and enrollments." "Java and Spring Boot" {
                tags "Server-side Application"
                iamModule = component "IAM Module" "Authentication, authorization, users, and roles." "Spring module"
                learningModule = component "Learning Module" "Courses, tutorials, students, and enrollments domain logic." "Spring module"
                profilesModule = component "Profiles Module" "Profile aggregate and profile-related operations." "Spring module"
                sharedModule = component "Shared Module" "Cross-cutting concerns: error handling, i18n, base abstractions, API docs config." "Spring module"
                
            }

            database = container "Learning Center Database" "Stores all domain data for IAM, profiles, courses, students, and enrollments." "Postgres Server" {
                tags "Database"
            }
        }

        // People -> products
        learner -> webApp "Visits" "HTTPS"
        learner -> iosApp "Uses" "HTTPS"
        learner -> androidApp "Uses" "HTTPS"
        instructor -> webApp "Visits" "HTTPS"
        admin -> webApp "Visits" "HTTPS"

        // Web Application delivers SPA assets to the browser
        webApp -> singlePageApp "Delivers static content and JavaScript" "HTTPS"

        // SPA and mobile apps call backend APIs
        singlePageApp -> learningApi "Signs in, manages courses, enrollments, and profiles" "JSON/HTTPS"
        iosApp -> learningApi "Signs in, views courses, tracks progress, and manages profile" "JSON/HTTPS"
        androidApp -> learningApi "Signs in, views courses, tracks progress, and manages profile" "JSON/HTTPS"

        // Learning Center REST API integrations
        learningApi -> database "Reads and writes" "JDBC"
        learningApi -> identityProvider "Validates identity tokens" "OIDC/OAuth2"
        learningApi -> videoPlatform "Retrieves media metadata and URLs" "HTTPS"
        learningApi -> emailProvider "Sends notifications" "HTTPS"
        learningApi -> paymentGateway "Processes payments" "HTTPS"

        // Internal module relationships (component level)
        iamModule -> sharedModule "Uses shared abstractions/utilities"
        learningModule -> sharedModule "Uses shared abstractions/utilities"
        profilesModule -> sharedModule "Uses shared abstractions/utilities"
        learningModule -> profilesModule "Reads profile identifiers through internal interfaces"
        profilesModule -> iamModule "Uses identity/user operations through internal interfaces"

        // Persistence access through repository adapters in each module
        iamModule -> database "Reads and writes IAM data"
        learningModule -> database "Reads and writes learning data"
        profilesModule -> database "Reads and writes profile data"

        // Client containers to components
        singlePageApp -> iamModule "Signs up and signs in" "JSON/HTTPS"
        singlePageApp -> learningModule "Manages courses, tutorials, and enrollments" "JSON/HTTPS"
        singlePageApp -> profilesModule "Manages profiles" "JSON/HTTPS"
        iosApp -> iamModule "Signs in" "JSON/HTTPS"
        iosApp -> learningModule "Views courses and tracks progress" "JSON/HTTPS"
        iosApp -> profilesModule "Views and updates profile" "JSON/HTTPS"
        androidApp -> iamModule "Signs in" "JSON/HTTPS"
        androidApp -> learningModule "Views courses and tracks progress" "JSON/HTTPS"
        androidApp -> profilesModule "Views and updates profile" "JSON/HTTPS"

        // Component to external system integrations
        iamModule -> identityProvider "Validates identity tokens" "OIDC/OAuth2"
        learningModule -> videoPlatform "Retrieves media metadata/URLs for learning content" "HTTPS"
        learningModule -> emailProvider "Sends enrollment and progress notifications" "HTTPS"
        learningModule -> paymentGateway "Authorizes and captures payments" "HTTPS"
    }

    views {
        systemContext solution "SystemContext" "Whole-solution context with external systems and actors." {
            include learner
            include instructor
            include admin
            include support
            include solution
            include identityProvider
            include paymentGateway
            include emailProvider
            include videoPlatform
            autoLayout lr
        }

        container solution "ContainerView" "Container view for the Learning Ecosystem Solution." {
            include learner
            include instructor
            include admin
            include support
            include webApp
            include singlePageApp
            include iosApp
            include androidApp
            include learningApi
            include database
            include identityProvider
            include paymentGateway
            include emailProvider
            include videoPlatform
            autoLayout lr
        }

        component learningApi "LearningApiComponents" "Component view of the Learning Center REST API." {
            include iamModule
            include learningModule
            include profilesModule
            include sharedModule
            include database
            include singlePageApp
            include iosApp
            include androidApp
            include identityProvider
            include paymentGateway
            include emailProvider
            include videoPlatform
            autoLayout lr
        }

        styles {
            element "Person" {
                shape Person
                background "#ffffff"
                color #0773af
                stroke #0773af
                fontSize 22
            }

            element "Software System" {
                background "#ffffff"
                color #0773af
                stroke #0773af
            }

            element "External System" {
                background "#999999"
                color "#ffffff"
            }

            element "Container" {
                background "#438dd5"
                color "#ffffff"
            }

            element "Server-side Application" {
                background "#ffffff"
                color #0773af
                stroke #0773af
                strokeWidth 7
                shape Shell
            }

            element "Directory" {
                shape Folder
                background "#ffffff"
                color #0773af
                stroke #0773af
            }

            element "Web Browser" {
                shape WebBrowser
                background "#ffffff"
                color #0773af
                stroke #0773af
            }

            element "Mobile App" {
                shape MobileDevicePortrait
                background "#ffffff"
                color #0773af
                stroke #0773af
            }

            element "Database" {
                shape Cylinder
                background "#ffffff"
                color #0773af
                stroke #0773af
                
            }

            element "Component" {
                background "#85bbf0"
                color "#000000"
            }
        }

        theme default
    }

    properties {
        structurizr.groupSeparator "/"
    }
}
