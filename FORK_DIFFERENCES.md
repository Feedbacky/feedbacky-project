This file is dedicated to show changes between self-hosted version and this fork which is used at https://app.feedbacky.net

## List of client-side changes
* [client/public/index.html](https://github.com/Feedbacky/feedbacky-project/blob/master/client/public/index.html)
    * Includes og and twitter meta tags
    * Keywords, description and title changed for service purposes
    * Includes Google Analytics
* [client/public/manifest.json](https://github.com/Feedbacky/feedbacky-project/blob/master/client/public/manifest.json)
    * Update short_name, name and keywords
* [client/src/App.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/App.js)
    * Replace `/admin/create` board create route with `/create`
* [client/src/assets/scss/utilities/colors.scss](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/assets/scss/utilities/colors.scss)
    * Include .text-blurred css
* [client/src/components/board/BoardInfoCard.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/board/BoardInfoCard.js)
    * Include Attribution.js
* [client/src/components/board/admin/AdminSidebar.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/board/admin/AdminSidebar.js)
    * Remove Feedbacky server/client version information
    * Include Attribution.js
* [client/src/components/commons/navbar-commons.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/commons/navbar-commons.js)
    * Remove isServiceAdmin check for board creation - allow everyone to create boards
* [client/src/components/profile/ProfileSidebar.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/components/profile/ProfileSidebar.js)
    * Include `/me/explore` page route
    * Include Attribution.js
* [client/src/routes/board/admin/subroutes/GeneralSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/admin/subroutes/GeneralSubroute.js)
    * Include API keys section to generate, enable and disable API Keys
* [client/src/routes/board/creator/CreatorBoardRoute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/creator/CreatorBoardRoute.js)
    * Remove isServiceAdmin check
    * Implement `You Must Be Logged To Do That` check
    * Remove banner and logo required check - they're optional
    * Implement 5 max created boards check
* [client/src/routes/board/creator/StepSecondSubroute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/board/creator/StepSecondSubroute.js)
    * Remove banner and logo requirement
    * Implement default banner and logo values
* [client/src/routes/profile/ProfileRoute.js](https://github.com/Feedbacky/feedbacky-project/blob/master/client/src/routes/profile/ProfileRoute.js)
    * Include `/me/explore` page route

## List of server-side changes
**TODO**