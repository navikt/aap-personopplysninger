rootProject.name = "personopplysninger"

include(
    "app",
    "libs:kafka",
    "libs:kafka-test",
    "libs:ktor-client-auth",
    "models:personopplysninger"
)
