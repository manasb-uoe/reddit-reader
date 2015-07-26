# reddit-reader-backbone
A minimalist single-page client for Reddit with an intuitive and responsive UI, developed using `Backbone.js`. *Reddit Reader* allows its users to: 
- Login to Reddit using OAuth 2.0 - Implicit Grant Flow (*Reddit Reader* will never ask you for your password)
- View subscribed, default and popular subreddits (and even jump to any subreddit you like)
- View post listings along with the ability to sort them by 'Hot', 'New', 'Rising', 'Controversial' and 'Top'
- View embedded post content (images and videos) by simply clicking on a post thumbnail
- View comments for any post along with the ability to sort them and jump between threads 
- Vote on posts and comments
- Seamlessly switch between light and dark themes

>**Note:** Since *Reddit Reader* authhenticates its users using implicit grant flow, it's not possible to refresh the access token once it has expired (60 minutes). On expiration, *Reddit Reader* will notify you and request you to re-authenticate. 

### Screenshots
Posts view: 
![Posts view](/screenshots/1.png?raw=true)

Comments view: 
![Comments view](/screenshots/2.png?raw=true)

Content viewer in action: 
![Content viewer in action](/screenshots/3.png?raw=true)

Optimized for small screen devices: 
![Optimized for small screen devices](/screenshots/5.png?raw=true)

Dark theme: 
![Dark theme](/screenshots/6.png?raw=true)

### How do I get set up? 
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

### Any queries?
Send me an email at [manas.bajaj94@gmail.com](manas.bajaj94@gmail.com)
