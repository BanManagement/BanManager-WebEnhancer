<p align="center">
  <a href="https://banmanagement.com">
    <img src="https://banmanagement.com/images/banmanager-icon.png" height="128">
    <h1 align="center">BanManager WebEnhancer</h1>
  </a>
</p>

<h3 align="center">
	Minecraft plugin required by <a href="https://github.com/BanManagement/BanManager-WebUI">BanManager-WebUI</a>
</h3>

<p align="center">
	<strong>
		<a href="https://banmanagement.com">Website</a>
		|
		<a href="https://banmanagement.com/docs/webui/install">Docs</a>
		|
		<a href="https://demo.banmanagement.com">Demo</a>
	</strong>
</p>
<p align="center">
  <a aria-label="Tests status" href="https://github.com/BanManagement/BanManager-WebEnhancer/actions/workflows/build.yml">
    <img alt="" src="https://img.shields.io/github/workflow/status/BanManagement/BanManager-WebEnhancer/Java%20CI?label=Tests&style=for-the-badge&labelColor=000000">
  </a>
  <a aria-label="License" href="https://github.com/BanManagement/BanManager-WebEnhancer/blob/master/LICENSE">
    <img alt="" src="https://img.shields.io/github/license/BanManagement/BanManager-WebEnhancer?labelColor=000&style=for-the-badge">
  </a>
  <a aria-label="Join the community on Discord" href="https://discord.gg/59bsgZB">
    <img alt="" src="https://img.shields.io/discord/664808009393766401?label=Support&style=for-the-badge&labelColor=000000&color=7289da">
  </a>
</p>

## Overview
- **Associate logs to reports.** Check what a player was doing in the moment
- **Seamless logins.** Generate 6 digit pins for player logins

To learn more about configuration, usage and features of BanManager, take a look at [the website](https://banmanagement.com/) or view [the demo](https://demo.banmanagement.com).

## Requirements
- Java 8+
- Minecraft server with [BanManager v7.7.0+](https://github.com/BanManagement/BanManager) & configured to [use MySQL or MariaDB](https://banmanagement.com/docs/banmanager/install#setup-shared-database-optional)

## Installation
- Download from https://ci.frostcast.net/job/BanManager-WebEnhancer/
- Copy jar to plugins (Spigot/BungeeCord) or mods (Sponge) folder
- Edit `BanManager/messages.yml` and add `[pin]` to token to the `ban.player.disallowed` & `tempban.player.disallowed messages`
  - It should similar to:
    ```yml
    ban:
      player:
      disallowed: '&6You have been banned from this server for &4[reason] Use [pin]'
    ```
- Grant players the permission `bm.command.bmpin`
- Restart the Minecraft server to enable the plugin

## Commands
- `/bmpin` - Display a valid login pin, requires `bm.command.bmpin` permission

## Development
```
git clone git@github.com:BanManagement/BanManager-WebEnhancer.git
```

## Contributing
If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

## Help / Bug / Feature Request
If you have found a bug please [open an issue](https://github.com/BanManagement/BanManager-WebEnhancer/issues/new) with as much detail as possible, including relevant logs and screenshots where applicable

Have an idea for a new feature? Feel free to [open an issue](https://github.com/BanManagement/BanManager-WebEnhancer/issues/new) or [join us on Discord](https://discord.gg/59bsgZB) to chat

## License
Free to use under the [MIT](LICENSE)
