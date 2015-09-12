# Reddit Reader - A minimalist Reddit client for Android and Web.

The project is divided into two directories: 

1. **frontend_android** - Android application 
2. **frontend_web** - Single-page web application 

###Android application 
An Android client for Reddit with an intuitive and sleek UI, targetting Android 4.1 (JELLY_BEAN) and above. *Reddit Reader* allows its users to: 

- Login to Reddit using OAuth 2.0 - Code flow (*Reddit Reader* will never ask you for your password). Since the access tokens are set to expire in 60 minutes, curent user's session is seamlessly refreshed as soon as it expires. 
- View subscribed subreddits' posts within a swipeable ViewPager, providing quick and easy navigation to all of user's favourite subreddits. Subreddits that appear within this ViewPager can also be customized by the user. 
- View post listings along with the ability to sort them by 'Hot', 'New', 'Rising', 'Controversial' and 'Top'
- Quickly view post content within an inbuilt web browser, wihout the need to leave the application at any point (except Youtube videos, which are opened directly in the Youtube app, if installed).
- View comments for any post along with the ability to sort them and jump between threads. 

####Screenshots: 
![Posts view](frontend_android/web_assets/1.png?raw=true)
![Posts view](frontend_android/web_assets/2.png?raw=true)

###Single-page web application
A minimalist single-page client for Reddit with an intuitive and responsive UI, developed using `Backbone.js`. *Reddit Reader* allows its users to: 
- Login to Reddit using OAuth 2.0 - Implicit Grant Flow (*Reddit Reader* will never ask you for your password)
- View subscribed, default and popular subreddits (and even jump to any subreddit you like)
- View post listings along with the ability to sort them by 'Hot', 'New', 'Rising', 'Controversial' and 'Top'
- View embedded post content (images and videos) by simply clicking on a post thumbnail
- View comments for any post along with the ability to sort them and jump between threads 
- Vote on posts and comments
- Seamlessly switch between light and dark themes

>**Note:** Since *Reddit Reader* authhenticates its users using implicit grant flow, it's not possible to refresh the access token once it has expired (60 minutes). On expiration, *Reddit Reader* will notify you and request you to re-authenticate. 

#### Screenshots
Posts view: 
![Posts view](frontend_web/screenshots/1.png?raw=true)

Comments view: 
![Comments view](frontend_web/screenshots/2.png?raw=true)

Content viewer in action: 
![Content viewer in action](frontend_web/screenshots/3.png?raw=true)

Optimized for small screen devices: 
![Optimized for small screen devices](frontend_web/screenshots/4.png?raw=true)

Dark theme: 
![Dark theme](frontend_web/screenshots/5.png?raw=true)

#### How do I get set up? 
After cloning the repository, run the following command from the root directory to install all dependencies: 
```
$ npm install 
```
Now minify all JavaScript modules into a single file `javascripts/build/main.min.js`: 
```
$ npm run build
```
Finally, start the localhost server on port 3000: 
```
$ npm start
```

