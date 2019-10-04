/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.config.update;

import com.thoughtworks.go.config.CruiseConfig;
import com.thoughtworks.go.config.SiteUrls;
import com.thoughtworks.go.config.commands.EntityConfigUpdateCommand;

public class CreateOrUpdateConfigServerSiteUrlsCommand implements EntityConfigUpdateCommand {
    private final SiteUrls newSiteUrls;
    private SiteUrls preprocessedSiteUrls;

    public CreateOrUpdateConfigServerSiteUrlsCommand(SiteUrls newSiteUrls) {
        this.newSiteUrls = newSiteUrls;
    }

    @Override
    public void update(CruiseConfig preprocessedConfig) throws Exception {
        preprocessedConfig.server().getSiteUrls().setSiteUrl(this.newSiteUrls.getSiteUrl());
        preprocessedConfig.server().getSiteUrls().setSecureSiteUrl(this.newSiteUrls.getSecureSiteUrl());
    }

    @Override
    public boolean isValid(CruiseConfig preprocessedConfig) {
        preprocessedSiteUrls = preprocessedConfig.server().getSiteUrls();
        return true;
    }

    @Override
    public void clearErrors() {
    }

    @Override
    public Object getPreprocessedEntityConfig() {
        return preprocessedSiteUrls;
    }

    @Override
    public boolean canContinue(CruiseConfig cruiseConfig) {
        return true;
    }
}