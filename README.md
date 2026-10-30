# JalebiSettings

Settings GUI + follow/friends system for **Jalebi SMP** (Paper 1.21.x, Java 21).

## Commands
| Command | What it does |
|---|---|
| `/settings` | Opens the settings menu |
| `/follow <player>` / `/unfollow <player>` | Follow / unfollow |
| `/friends`, `/followers`, `/following` | Open the 3-tab GUI on that tab |

**Friend** = you follow them *and* they follow you back.
**"Friends/Followed"** in a setting = players *you* follow (friends are included).

## Build
GitHub Actions builds automatically on every push (Actions -> latest run -> Artifacts).
Tag `v1.0.0` to get a GitHub Release with the jar. Local: `mvn package`.

## Optional dependency
ProtocolLib is needed for: Server Hotbar Messages, Totem Particles, Explosion Particles, Explosion Sounds.

## Hooking your other plugins (pay, bounty, auction, ...)
```java
import com.jalebismp.settings.SettingsAPI;
import com.jalebismp.settings.data.Setting;

if (SettingsAPI.isOn(player.getUniqueId(), Setting.PAY_ALERTS)) { /* send alert */ }
if (SettingsAPI.allows(receiverUuid, senderUuid, Setting.ALLOW_PAYMENTS)) { /* ok */ }
```
