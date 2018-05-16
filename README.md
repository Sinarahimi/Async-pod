
# Pod-Async
A webSocket helepr that works with Fanap's POD Async service (DIRANA)

## Getting Started

This library allows you to connect to **Pod-Async** and use their services.

### Prerequisites

What things you need to Add this module to your project and after that set the `internet` permision in the manifest.
Then you need to getInstance of the Async library in the `Oncrete()`.


``` @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Async async = Async.getInstance(context);
```

#The table below is the list of  methods defined in Async class

| Method                        | Description                                          |
|:------------------------------|:-----------------------------------------------------|
| `connect(String socketServerAddress, String appId)`         | Called when an `onXxx()` method threw a `Throwable`. |
| `sendMessage(String textMessage, int messageType)`               | Called when a binary frame was received.             |
| `getLiveState()`             | Called when a binary message was received.           |
| `getState()`                | Called when a close frame was received.              |
| `getErrorMessage()`                 | Called after the opening handshake succeeded.        |
| `isSocketOpen()`              | Called when `connectAsynchronously()` failed.        |
| `getPeerId()`         | Called when a continuation frame was received.       |

## Built With

* [moshi](https://github.com/square/moshi) - Moshi
* [websocket-client](https://github.com/TakahikoKawasaki/nv-websocket-client) - Websocket
* [lifecycle](https://developer.android.com/reference/android/arch/lifecycle/LiveData) - LiveData

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


