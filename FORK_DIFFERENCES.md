This file is dedicated to show changes between self-hosted version and this fork which is used at https://app.feedbacky.net

## List of docker changes
* [docker-compose.yml](https://github.com/Feedbacky/feedbacky-project/blob/master/docker-compose.yml)
    * Includes old download to compile way to use Feedbacky instead Docker Images
* [client/Dockerfile](https://github.com/Feedbacky/feedbacky-project/blob/master/client/Dockerfile)
    * Merged with proxy/Dockerfile
    * Includes old download to compile way to use Feedbacky instead Docker Images

## List of client-side changes
* [client/public/index.html](https://github.com/Feedbacky/feedbacky-project/blob/master/client/public/index.html)
    * Includes og and twitter meta tags
    * Keywords, description and title changed for service purposes
    * Remove env-config.js usage
* [client/public/manifest.json](https://github.com/Feedbacky/feedbacky-project/blob/master/client/public/manifest.json)
    * Update short_name, name and keywords
* [client/src/App.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/App.js)
    * Replace `/admin/create` board create route with `/create`
    * Includes [Ackee analytics](https://github.com/electerious/Ackee) and useAckee hook
* [client/src/assets/scss/utilities/colors.scss](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/assets/scss/utilities/colors.scss)
    * Include .text-blurred css
* [client/src/components/board/BoardInfoCard.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/board/BoardInfoCard.js)
    * Include Attribution.js
* [client/src/components/board/IdeaCreateModal.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/board/IdeaCreateModal.js)
    * Include Ackee events analytics
* [client/src/components/board/admin/AdminSidebar.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/board/admin/AdminSidebar.js)
    * Remove Feedbacky server/client version information
    * Include Attribution.js
* [client/src/components/commons/navbar-commons.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/commons/navbar-commons.js)
    * Remove isServiceAdmin check for board creation - allow everyone to create boards
* [client/src/components/commons/VoteButton.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/commons/VoteButton.js)
    * Include Ackee events analytics
* [client/src/components/profile/ProfileSidebar.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/profile/ProfileSidebar.js)
    * Include `/me/explore` page route
    * Include Attribution.js
* [client/src/routes/board/admin/subroutes/webhooks/WebhooksSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/admin/subroutes/webhooks/WebhooksSubroute.js)
    * Include webhooks quotas (from unlimited to 10)
* [client/src/routes/board/admin/subroutes/GeneralSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/admin/subroutes/GeneralSubroute.js)
    * Include API keys section to generate, enable and disable API Keys
* [client/src/routes/board/admin/subroutes/ModeratorsSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/admin/subroutes/ModeratorsSubroute.js)
    * Include moderators quotas (from unlimited to 15)
* [client/src/routes/board/admin/subroutes/TagsSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/admin/subroutes/TagsSubroute.js)
    * Include tags quotas (from unlimited to 25)
* [client/src/routes/board/creator/CreatorBoardRoute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/creator/CreatorBoardRoute.js)
    * Remove isServiceAdmin check
    * Implement `You Must Be Logged To Do That` check
    * Remove banner and logo required check - they're optional
    * Implement 5 max created boards check
* [client/src/routes/board/creator/StepSecondSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/creator/StepSecondSubroute.js)
    * Remove banner and logo requirement
    * Implement default banner and logo values
* [client/src/routes/LoginRoute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/LoginRoute.js)
    * Include Ackee events analytics
* [client/src/utils/env-vars.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/utils/env-vars.js)
    * Remove window._env_ usage

## List of server-side changes
**TODO**