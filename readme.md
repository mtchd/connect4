# Connect 4

This is a bot that runs Connect 4 in Slack. All input is entirely facilitated through messages!

Here's an example of typical output:

<img width="229" alt="Screen Shot 2019-08-15 at 10 32 10 am" src="https://user-images.githubusercontent.com/30946820/63065131-33419f80-bf48-11e9-8483-b477fae5285a.png">

Here's a video of a game:

[![video-thumbnail](https://user-images.githubusercontent.com/30946820/71894603-d7034480-31a2-11ea-9497-1488ba41b20e.PNG)](https://www.youtube.com/watch?v=TlFrnz3iKWk)

Game also runs in console for testing!

## Tech Stack
- Scala App
- Uses the slack scala client here to handle interactions with slack - https://github.com/slack-scala-client/slack-scala-client
- AWS as the cloud provider
- Terraform for Infrastructure as Code
- Buildkite for it's CI pipeline
- Docker for containerization

## Working with Connect 4

### Deployment

All scripts for deployment are under the `auto` folder.

1. The app is packaged and is pushed to Docker Hub with `auto/build-push`
1. AWS infrastructure is created with `auto/terraform`

After that, the app is running.

### Local Development

The App can be run locally with `auto/build-dev`, which will build the docker image then run it locally.

## Basic Usage

Connect 4 accepts the following commands:
 - Help
 - Score
 - Challenge
 - Accept
 - Drop
 - Reject
 - Token
 - Forfeit
 
 Try them out and see what happens!
 
## License

Licensed under the GNU General Public License v3.0. Feel free to use parts of the code in your own projects with attribution!
