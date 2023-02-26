# ChatGPT-Swing

**[中文](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing.html)**

**ChatGPT-Swing** is an unofficial GUI using Java Swing technology and is based on the web version of
ChatGPT (3.5) rather than the public GPT-3 API.

With this program, you can do almost everything that can be done on the web version, like:

- user login (only normal account is supported currently, not Google or Microsoft account), with proxy support
- accessToken auto refresh (before 1 day of expiration)
- make conversation (support context association)
- delete conversation
- rename conversation title
- clear all conversations
- give a feedback (thumbsUp or thumbsDown) on response
- speech-to-text (using Free TTS API for testing with an IP based limitation of 6000 tokens per week)

The API for this application which is developed with Golang and Gin is available
here: [go-chatgpt-api](https://github.com/linweiyuan/go-chatgpt-api), you can build and deploy your own in supported
countries.

**Please note: this is an unofficial application using unofficial APIs, maybe unavailable at any time.**

---

Screenshots:

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/login.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/Linux.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/syntax_highlight.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/macOS.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/conversation_menu.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/rename_conversation.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/delete_conversation.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/message_menu.png)

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/feedback.png)

---

Thanks to these libraries.

![](https://linweiyuan.github.io/2023/02/25/ChatGPT-Swing/dependencies.png)