LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := FaceUnlockServiceAndroidStudio
LOCAL_MODULE_CLASS := FAKE
LOCAL_MODULE_SUFFIX := -timestamp

faceunlock_framework_deps := $(call java-lib-deps,faceunlock_framework)
faceunlock_vendor_framework_deps := $(call java-lib-deps,faceunlock_vendor_framework)
faceunlock_android_framework_deps := $(call java-lib-deps,framework)

prebuilt_libs_path := $(abspath $(LOCAL_PATH))/../../../external/faceunlock/prebuilt/lib64

faceunlock_libs_path := $(abspath $(LOCAL_PATH))/app/libs
faceunlock_system_libs_path := $(abspath $(LOCAL_PATH))/app/system_libs
faceunlock_native_libs_path := $(abspath $(LOCAL_PATH))/app/src/main/jniLibs/arm64-v8a

include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): $(faceunlock_framework_deps) $(faceunlock_vendor_framework_deps) $(faceunlock_android_framework_deps)
	$(hide) mkdir -p $(faceunlock_libs_path)
	$(hide) mkdir -p $(faceunlock_system_libs_path)
	$(hide) mkdir -p $(faceunlock_native_libs_path)
	$(hide) rm -rf $(faceunlock_libs_path)/*.jar
	$(hide) rm -rf $(faceunlock_system_libs_path)/*.jar
	$(hide) cp $(faceunlock_framework_deps) $(faceunlock_libs_path)/faceunlock_framework.jar
	$(hide) cp $(faceunlock_vendor_framework_deps) $(faceunlock_libs_path)/faceunlock_vendor_framework.jar
	$(hide) cp $(faceunlock_android_framework_deps) $(faceunlock_system_libs_path)/framework.jar
	$(hide) cp $(prebuilt_libs_path)/*.so $(faceunlock_native_libs_path)
	$(hide) echo "Fake: $@"
	$(hide) mkdir -p $(dir $@)
	$(hide) touch $@