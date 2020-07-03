// IRecommendationServiceCallback.aidl
package org.droidtv.defaultdashboard.recommended;

import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;

// Declare any non-default types here with import statements

interface IRecommendationServiceCallback {
    /**
             * Function called on registration success.<br> Only after successful registration clients can call function {@link IRecommendation#getAllRecommendations()}
             * to get the list of available recommendations or {@link IRecommendation#cancelRecommendation} to cancel any recommendation
             * @tt.wrapper Asynchronous
             */
            void onRegistrationSuccess();
            void onRecommendationChanged(in Recommendation recommendation, in RecommendationChangeType type);
}
