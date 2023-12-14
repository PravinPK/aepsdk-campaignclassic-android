/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/
package com.adobe.marketing.mobile;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.google.android.gms.common.util.CollectionUtils;
import java.util.List;

public class CarouselTemplateNotificationBuilder {
    private static final String SELF_TAG = "CarouselTemplateNotificationBuilder";

    @NonNull static NotificationCompat.Builder construct(
            final CarouselPushTemplate pushTemplate, final Context context)
            throws NotificationConstructionFailedException {
        final String channelId =
                AEPPushNotificationBuilder.createChannelAndGetChannelID(pushTemplate, context);
        final String packageName =
                ServiceProvider.getInstance()
                        .getAppContextService()
                        .getApplication()
                        .getPackageName();

        final String carouselOperationMode = pushTemplate.getCarouselOperationMode();

        NotificationCompat.Builder builder;
        if (carouselOperationMode.equals(CampaignPushConstants.DefaultValues.AUTO_CAROUSEL_MODE)) {
            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Building an auto carousel push notification.");
            builder =
                    AutoCarouselTemplateNotificationBuilder.construct(
                            pushTemplate, context, channelId, packageName);
        } else {
            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a manual carousel push notification.");
            builder =
                    buildManualCarouselNotification(pushTemplate, context, channelId, packageName);
        }

        return builder;
    }

    static NotificationCompat.Builder buildManualCarouselNotification(
            final CarouselPushTemplate pushTemplate,
            final Context context,
            final String channelId,
            final String packageName)
            throws NotificationConstructionFailedException {
        final String carouselLayoutType = pushTemplate.getCarouselLayoutType();
        if (carouselLayoutType.equals(
                CampaignPushConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE)) {
            return FilmstripCarouselTemplateNotificationBuilder.construct(
                    pushTemplate, context, channelId, packageName);
        }
        return ManualCarouselTemplateNotificationBuilder.construct(
                pushTemplate, context, channelId, packageName);
    }

    static NotificationCompat.Builder fallbackToBasicNotification(
            final Context context,
            final CarouselPushTemplate pushTemplate,
            final List<String> downloadedImageUris,
            final int minimumImageCount)
            throws NotificationConstructionFailedException {
        Log.trace(
                CampaignPushConstants.LOG_TAG,
                SELF_TAG,
                "Only %d image(s) for the carousel notification were downloaded while at least %d"
                        + " were expected. Building a basic push notification instead.",
                downloadedImageUris.size(),
                minimumImageCount);
        if (!CollectionUtils.isEmpty(downloadedImageUris)) {
            // use the first downloaded image (if available) for the basic template notification
            pushTemplate.modifyData(
                    CampaignPushConstants.PushPayloadKeys.IMAGE_URL, downloadedImageUris.get(0));
        }
        final BasicPushTemplate basicPushTemplate = new BasicPushTemplate(pushTemplate.getData());
        return BasicTemplateNotificationBuilder.construct(basicPushTemplate, context);
    }
}
