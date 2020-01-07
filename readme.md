# Connect 4

This is a bot that runs connect 4 in Slack. All input is entirely facilitated through messages!

Here's an example of typical output:

<img width="229" alt="Screen Shot 2019-08-15 at 10 32 10 am" src="https://user-images.githubusercontent.com/30946820/63065131-33419f80-bf48-11e9-8483-b477fae5285a.png">

Here's a video of fairly exciting game:

[![video-thumbnail](https://user-images.githubusercontent.com/30946820/71894603-d7034480-31a2-11ea-9497-1488ba41b20e.PNG)](https://www.youtube.com/watch?v=TlFrnz3iKWk)

Game also runs in console for testing!

## Tech Stack
- Scala App
- Uses the slack scala client here to handle interactions with slack - https://github.com/slack-scala-client/slack-scala-client
- AWS as the cloud provider
- Terraform for Infrastructure as Code
- Buildkite for it's CI pipeline
- Docker for containerization

## License

Licensed under the GNU General Public License v3.0. Feel free to use parts of the code in your own projects with attribution!
