# CXF, writing headers after body

This repository contains tests to mess around with writing headers after the body.

The main idea is that some API's require an `Authorization`-header that depends on the body. For example `Authorization: MyApp <base64(sha1(body + secret key))>`.


## Scenarios

There are three scenarios set up:

 * Chunking allowed, where the size of the body **does not** exceed the chunking threshold
 * Chunking allowed, where the size of the body **does** exceed the chunking threshold
 * Chunking disabled

Only the first scenario actually works. After messing around a bit with this, I understand why this happens. CXF obviously wants to write directly to the output stream as soon as possible, and it makes no sense to write headers after that point. The purpose of this repository is to find a decent solution or workaround. For now, I've simply raised the chunking threshold to get around the problem in my production environment, however it doesn't feel like an okay work around.


## License

Apache License © [Anton Johansson](https://github.com/anton-johansson)
