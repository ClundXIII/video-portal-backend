# video-portal-backend
Backend for the video portal

# build
Copy `src/test/resources/credentials-template.json` to `src/test/resources/credentials.json` and fill in your google api and vimeo api key.
The oauth2 key for vimeo ist not needed, you can leave that blank. Then run `mvn install` and put the lib/ folder alongside your java file.
Start it with `java -jar video-portal-backend.jar` and follow the instructions.
