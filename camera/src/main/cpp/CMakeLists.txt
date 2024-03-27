# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
if (${ANDROID_ABI} STREQUAL "x86_64")
    include_directories(${ANDROID_SYSROOT}/usr/include/x86_64-linux-android)
elseif (${ANDROID_ABI} STREQUAL "x86")
    include_directories(${ANDROID_SYSROOT}/usr/include/i686-linux-android)
elseif (${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
elseif (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()

add_library( # Sets the name of the library.
        NdkGlue

         # Sets the library as a shared library.
         SHARED

         # Provides a relative path to your source file(s).
         GLES2Lesson.cpp

         NdkGlue.cpp

        FilterPreProcessor.cpp




       # processor.cpp

        RenderedShader.cpp
        AquaShader.cpp
        AnimationShader.cpp







        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.




find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

set(OPENGL_LIB GLESv3)



# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

set(CMAKE_CXX_FLAGS "-std=c++11")

target_link_libraries( # Specifies the target library.
                         NdkGlue

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}

                    ${OPENGL_LIB}
                    android
                    EGL
                    m
                    jnigraphics

        )