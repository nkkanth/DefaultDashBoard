#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

ifneq (,$(filter userdebug eng,$(TARGET_BUILD_VARIANT)))
   LOCAL_DEX_PREOPT := false
endif

#----START PREBUILD ----
include $(CLEAR_VARS)

PATH_ANDROID_X_LIBS := $(TOP)/../../../../../../../../../prebuilts/sdk/current/androidx/m2repository/androidx
PATH_ANDROID_SUPPORT_LIBS := $(TOP)/../../../../../../../../../prebuilts/sdk/current/support/m2repository/

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
			ddb-jedijar:libs/jedijar.jar \
                        ddb-hotspot:libs/hotspot.jar \
                        ddb-qrcore:libs/qrcode/core-2.4.0.jar \
                        ddb-qrcore3:libs/qrcode/core-3.3.0.jar \
	                ddb-qrandroid:libs/qrcode/android-2.4.0.jar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += \
			support-annotations:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-annotations/28.0.0-rc01/support-annotations-28.0.0-rc01.jar \
			support-v4:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-v4/28.0.0-rc01/support-v4-28.0.0-rc01.aar \
			support-media-compat:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-media-compat/28.0.0/support-media-compat-28.0.0.aar \
			support-core-ui:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-core-ui/28.0.0-rc01/support-core-ui-28.0.0-rc01.aar \
			support-core-utils:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-core-utils/28.0.0-rc01/support-core-utils-28.0.0-rc01.aar \
			support-fragment:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-fragment/28.0.0-rc01/support-fragment-28.0.0-rc01.aar
			#support-compat:$(PATH_ANDROID_SUPPORT_LIBS)/com/android/support/support-compat/28.0.0/support-compat-28.0.0.aar \

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += \
			ddb-androidx-leanback:$(PATH_ANDROID_X_LIBS)/leanback/leanback/1.1.0-alpha01/leanback-1.1.0-alpha01.aar \
		        ddb-androidx-recyclerview:$(PATH_ANDROID_X_LIBS)/recyclerview/recyclerview/1.1.0-alpha01/recyclerview-1.1.0-alpha01.aar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += \
			ddb-play-services-auth:libs/google/play-services-auth-17.0.0.aar \
			ddb-play-services-auth-api-phone:libs/google/play-services-auth-api-phone-17.1.0.aar \
			ddb-play-services-auth-base:libs/google/play-services-auth-base-17.0.0.aar \
			ddb-play-services-base:libs/google/play-services-base-17.1.0.aar \
			ddb-play-services-basement:libs/google/play-services-basement-17.1.0.aar \
            ddb-play-services-tasks:libs/google/play-services-tasks-17.0.0.aar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += \
		        ddb-androidx-annotations:$(PATH_ANDROID_X_LIBS)/annotation/annotation/1.0.0-rc01/annotation-1.0.0-rc01.jar \
			ddb-androidx-core:$(PATH_ANDROID_X_LIBS)/core/core/1.1.0-alpha01/core-1.1.0-alpha01.aar \
			ddb-androidx-localbroadcastmanager:$(PATH_ANDROID_X_LIBS)/localbroadcastmanager/localbroadcastmanager/1.0.0-rc01/localbroadcastmanager-1.0.0-rc01.aar \
	 	        ddb-androidx-fragment:$(PATH_ANDROID_X_LIBS)/fragment/fragment/1.0.0-rc01/fragment-1.0.0-rc01.aar \
			ddb-androidx-lifecycle-common:$(PATH_ANDROID_X_LIBS)/lifecycle/lifecycle-common/2.0.0-rc01/lifecycle-common-2.0.0-rc01.jar \
			ddb-androidx-lifecycle-viewmodel:$(PATH_ANDROID_X_LIBS)/lifecycle/lifecycle-viewmodel/2.0.0-rc01/lifecycle-viewmodel-2.0.0-rc01.aar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=\
		        ddb-androidx-core-common:$(PATH_ANDROID_X_LIBS)/arch/core/core-common/2.0.0-rc01/core-common-2.0.0-rc01.jar

include $(BUILD_MULTI_PREBUILT)
#----END PREBUILD ----

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := android-common 
LOCAL_RES_LIBRARIES := tvwidgets2k15 tvstrings htvstrings
LOCAL_JAVA_LIBRARIES := htvfw tvfw
LOCAL_JAVA_LIBRARIES += com.mediatek.twoworlds.tv
LOCAL_SRC_FILES := $(call all-java-files-under, java) \
					$(call all-java-files-under, src) \
					$(call all-Iaidl-files-under, java) \
                   src/org/droidtv/weather/WeatherBinder.aidl \
                   src/org/droidtv/weather/WeatherCallback.aidl
                   #src/org/droidtv/defaultdashboard/recommended/IRecommendationService.aidl \
                   src/org/droidtv/defaultdashboard/recommended/IRecommendationServiceCallback.aidl \

LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/java

$(warning $(LOCAL_SRC_FILES))

LOCAL_RESOURCE_DIR := \
                     $(LOCAL_PATH)/res

LOCAL_STATIC_ANDROID_LIBRARIES := \
		  androidx.collection_collection \
		  androidx.lifecycle_lifecycle-common \
		  androidx.lifecycle_lifecycle-common-java8 \
		  androidx.lifecycle_lifecycle-viewmodel \
		  androidx.lifecycle_lifecycle-runtime \
		  androidx.lifecycle_lifecycle-livedata \
		  androidx.lifecycle_lifecycle-livedata-core \
		  androidx.lifecycle_lifecycle-service \
		  androidx.lifecycle_lifecycle-process \
		  androidx.lifecycle_lifecycle-extensions \
		  ddb-androidx-annotations \
		  ddb-androidx-core-common \
		  support-annotations \

LOCAL_STATIC_JAVA_AAR_LIBRARIES := \
   		  ddb-androidx-leanback \
		  ddb-androidx-recyclerview \
		  ddb-androidx-core \
		  ddb-androidx-localbroadcastmanager \
		  ddb-androidx-fragment
		  #ddb-support-media-compat

LOCAL_STATIC_JAVA_AAR_LIBRARIES += \
                  ddb-play-services-auth \
                  ddb-play-services-auth-api-phone \
                  ddb-play-services-auth-base \
                  ddb-play-services-base \
                  ddb-play-services-basement \
                  ddb-play-services-tasks

LOCAL_STATIC_JAVA_AAR_LIBRARIES += \
		  support-v4 \
		  support-media-compat \
		  support-core-ui \
		  support-core-utils \
		  support-fragment
		  #support-compat \
		  
LOCAL_AAPT_FLAGS := --auto-add-overlay \
		  --extra-packages androidx.leanback \
	          --extra-packages androidx.recyclerview \
		  --extra-packages com.google.android.gms.auth.api \
   		  --extra-packages com.google.android.gms \
		  --extra-packages com.google.android.gms.tasks \
		  --extra-packages com.google.android.gms.auth
	
LOCAL_PACKAGE_NAME := DefaultDashboard
LOCAL_CERTIFICATE := platform

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_STATIC_JAVA_LIBRARIES += \
                  ddb-jedijar \
                  ddb-hotspot \
                  ddb-qrcore \
                  ddb-qrandroid \
                  ddb-qrcore3 

include $(BUILD_PACKAGE)


include $(call all-makefiles-under,$(LOCAL_PATH))
