import org.jetbrains.kotlin.gradle.dsl.JvmTarget
@Suppress("SuspiciousIndentation")


//Archivo de arquitectura de construcción de Gradle para el proyecto
//Configuramos características y librerias que usaremos en él

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.tarea1"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.tarea1" //ID de la app
        minSdk = 24 //Versión mínima de Android donde funciona
        targetSdk = 36 //Versión de Android donde se ha probado y optimizado
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        // Este bloque 'compilerOptions' es la forma moderna que reemplaza a 'kotlinOptions'
        compilerOptions {
            // Esta línea resuelve la advertencia de 'deprecated' de jvmTarget
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    //ViewBinding
    viewBinding { //Activa la vinculación de vista Android con Kotlin, binding en lugar de findViewByID
        enable=true
    }
}

@Suppress("UseTomlInstead")
dependencies {
    //Componentes básicos
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout) //Sistema de diseño XML elegido
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Componente Navigation para la navegación entre Fragments
    // Gestiona el flujo entre LoginFragment y RegisterFragment
    // Requisito de la tarea
    // Utiliza nav_graph.xml
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
    // Componente Material Design
    // Necesario para utilizar TextInputLayout
    // PORQUÉ: Requisito de la tarea
    implementation("com.google.android.material:material:1.13.0")

    // Componente Lifecycle y ViewModel para separar la lógica de verificación de contraseña, de la interfaz
    // ViewModel separa la lógica (model) de la vista (view) - MVVM
    // LiveData se usa para que ViewModel avise al fragment cuando cambia el estado de forma reactiva
    // Requisito de la tarea
    val lifecycleVersion = "2.10.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Componente Fragment KTX, extensiones de Kotlin para Fragments
    // Permite usar 'by viewModels()' - código simplificado
    // Requisito de la tarea
    implementation("androidx.fragment:fragment-ktx:1.8.9")
}