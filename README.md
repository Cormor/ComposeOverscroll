# ComposeOverscroll
Overscroll any scrollable items!

## Preview
### compare with iOS
![overscrollIOS](https://user-images.githubusercontent.com/21119517/206082779-0c98ae17-b54c-4088-bec4-4c2e0cfd5672.gif)

### demo
https://user-images.githubusercontent.com/21119517/205905168-180bc330-85c0-4fbe-a712-8a999e398cb4.mp4

#### The 1.4.0-beta02 version has a serious nested gesture dispatch error.
####  1.4.0-beta02版本存在严重的嵌套手势分发错误
#### You can +1 in the issue at the end of this article
#### 您可以在本文末尾的issue链接中+1帮助google重视此问题

## How to use

### for column (is Testing at compose 1.5.0,1.3.0 is useful):
```
Column(Modifier
        .fillMaxSize()
        .overScrollVertical() // invoke before the scrollable Modifier
        .verticalScroll(rememberScrollState())
        ) { ... }
```
### for scrollable Composable like lazyColumn :
```
  val scrollState = rememberLazyListState()
        
        LazyColumn(Modifier
            .fillMaxWidth()
            .overScrollVertical(), // * u should do it
            state = scrollState, // * u should do it
            flingBehavior = rememberOverscrollFlingBehavior { scrollState } // * u should do it
        ) {
            // ...
        }
```
### U can call it nested! pls see the demo!

This is an issue submitted to Google about this project:
https://issuetracker.google.com/issues/276682419
https://issuetracker.google.com/issues/261895103
