---
title: Auth
date: 2024-06-17 10:55:00
categories: [ Programing Case ]
---

### Session ID

SessionID的用户内容是放在服务器中(分布式缓存 Redis)

### JWT

JWT（JSON Web Token）中的信息是否加密，取决于JWT的具体实现和使用方式。一般来说，JWT由三部分组成：头部（Header）、负载（Payload）和签名（Signature）。

1.头部（Header）：这部分包含了JWT的元数据，如所使用的签名算法等。头部通常使用Base64Url编码，但请注意，Base64编码并不等同于加密。它只是将二进制数据转换为ASCII字符串格式，以便在URL和HTTP请求头等场景中安全地传输。

2.负载（Payload）：这部分包含了JWT的具体内容，如用户ID、角色、权限等。负载同样使用Base64Url编码，并且默认情况下并不加密。这意味着如果JWT被截获，其内容可以被任何知道Base64解码的人读取。因此，不要在负载中放置敏感信息，除非这些信息是公开的或可以被任何人访问的。

3.签名（Signature）：签名部分用于验证JWT的完整性和真实性。它通过对头部和负载进行编码后的字符串，加上一个密钥（Secret），使用指定的签名算法（如HMAC
SHA256、RSA等）进行加密生成。这个密钥只有服务器知道，因此只有服务器才能验证JWT的签名是否有效。如果JWT被篡改，其签名将不再匹配，从而可以被服务器检测到。