package org.droidtv.defaultdashboard.recommended;

import org.droidtv.defaultdashboard.recommended.IRecommendationServiceCallback;
import org.droidtv.defaultdashboard.recommended.Recommendation;

interface IRecommendationService {

    void registerCallback(IRecommendationServiceCallback listener);

    void unRegisterCallback(IRecommendationServiceCallback listener);

    List<Recommendation> getAllRecommendations();

    void cancelRecommendation(in Recommendation recommendation);
}
