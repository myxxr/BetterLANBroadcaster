# BetterLANBroadcaster

一个 Spigot 插件，通过 UDP 多播协议在局域网中广播 Minecraft 服务器，使客户端无需手动输入地址即可发现服务器。

## 功能

- 遵循 Minecraft 局域网发现协议，向 `224.0.2.60:4445` 发送服务器信息
- 支持通过命令动态启停广播
- 支持在线修改广播 MOTD 和广播延迟
- 支持查看当前广播状态
- 支持自定义广播端口（手动指定或自动获取）
- 支持调试模式，实时查看广播包发送日志
- 支持多语言（英文、中文）
- 所有配置热重载

## 命令

| 命令 | 说明 |
|------|------|
| `/blb` | 显示插件版本信息 |
| `/blb start` | 启动广播 |
| `/blb stop` | 停止广播 |
| `/blb status` | 查看广播状态 |
| `/blb setmotd <MOTD>` | 设置广播 MOTD |
| `/blb setdelay <毫秒>` | 设置广播延迟 |
| `/blb setport <端口|auto>` | 设置广播端口（使用 auto 自动获取） |
| `/blb debug <on|off>` | 开启/关闭调试模式 |
| `/blb reload` | 重载配置文件 |
| `/blb help` | 显示所有子命令帮助 |
| `/blb version` | 显示插件版本信息 |

> 提示：`/betterlanbroadcaster` 是完整命令名，`/blb` 为其别名，两者等效。

所有命令需要 `betterlanbroadcaster.admin` 权限，默认仅 OP 可用。

## 配置文件

`plugins/BetterLANBroadcaster/config.yml`

```yaml
# 语言设置: en (英语) 或 zh (中文)
language: en

# 局域网显示的服务端 MOTD
motd: "A Minecraft Server"

# 广播延迟（毫秒）
# 默认: 1500 ms = 1.5 秒
broadcast-delay-ms: 1500

# 调试模式: true 开启后每次发送广播包将在控制台输出日志
debug: false

# 广播端口: 设为 0 自动获取服务器端口，或指定具体端口号
broadcast-port: 0
```

## 多语言

语言文件位于 `plugins/BetterLANBroadcaster/lang/` 目录：

- `messages_en.yml` - 英文语言文件
- `messages_zh.yml` - 中文语言文件

在 `config.yml` 中将 `language` 设置为 `en` 或 `zh` 切换语言。

## 局域网发现协议说明

插件实现的协议与 Minecraft 原版局域网广播完全兼容：

- 协议: UDP 多播
- 地址: `224.0.2.60`
- 端口: `4445`
- 消息格式: `[MOTD]服务端MOTD[/MOTD][AD]服务端端口[/AD]`
- 编码: UTF-8

## 构建

```bash
git clone https://github.com/myxxr/BetterLANBroadcaster.git
cd BetterLANBroadcaster
mvn clean package
```

构建产物位于 `target/BetterLANBroadcaster-1.0.0.jar`。

## 依赖

- Spigot 1.20.4+ API
- Java 17+

## 安装

1. 将 `BetterLANBroadcaster-1.0.0.jar` 放入 `plugins/` 目录
2. 启动服务器
3. 使用 `/blb start` 命令启动广播
4. 局域网中的 Minecraft 客户端进入多人游戏即可发现服务器

## 作者

Immyxxr
- Email: myxxr1999@163.com
- QQ: 2855848368

## 开源协议

本项目采用 GNU General Public License v3.0 (GPL v3) 开源协议。详细信息请参阅 [LICENSE](LICENSE) 文件。
