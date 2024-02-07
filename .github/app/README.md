# dotty-bot

> A GitHub App built with [Probot](https://github.com/probot/probot) that Bot for Dotty (as a GitHub App)

## Setup

```sh
# Install dependencies
npm install

# Run the bot
npm start
```

## Docker

```sh
# 1. Build container
docker build -t dotty-bot .

# 2. Start container
docker run -e APP_ID=<app-id> -e PRIVATE_KEY=<pem-value> dotty-bot
```

## Contributing

If you have suggestions for how dotty-bot could be improved, or want to report a bug, open an issue! We'd love all and any contributions.

For more, check out the [Contributing Guide](CONTRIBUTING.md).

## License

[ISC](LICENSE) © 2024 lampepfl
