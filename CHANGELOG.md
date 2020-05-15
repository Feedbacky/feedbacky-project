### 0.2.0-beta (X, 2020)
* Fixed bug with board admin panel losing theme color after page refresh
* Removed MDBootstrap dependency (client CSS size reduced)
* Moved css to sass and compiling bootstrap there to reduce unused css (client CSS size reduced)
* Client size assets (SVGs, images) are now stored in src/assets folder not at cdn.feedbacky.net
* Minor UI changes including:
  * Special comments icons change
  * Board admin panel and profile page UI changes
* Added mail notifications feature, subscribe to idea to receive mails when it gets changed, supported notifications:
  * Idea commented by board moderator
  * Idea tags changed
  * Idea status changed (closed, opened)
* Comments are now loaded from oldest