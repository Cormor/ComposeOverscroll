# ComposeOverscroll
Overscroll any scrollable items!

## Preview
### compare with iOS
![overscrollIOS](https://user-images.githubusercontent.com/21119517/206082779-0c98ae17-b54c-4088-bec4-4c2e0cfd5672.gif)

### demo
https://user-images.githubusercontent.com/21119517/205905168-180bc330-85c0-4fbe-a712-8a999e398cb4.mp4

#### The 1.4.0-beta02 version has a serious nested gesture dispatch error.You can find a failed test case in the test module.
####  1.4.0-beta02版本起，存在严重的嵌套手势分发错误，您可以在测试模块中找到失败的测试用例。
#### You can +1 in the issue at the end of this article
#### 您可以在本文末尾的issue链接中+1帮助google重视此问题

## How to use

### for column :
```
Column(Modifier
    .fillMaxSize()
    .overScrollVertical() // invoke before the scrollable Modifier
    //.overScrollHorizontal() // or this
    .verticalScroll(state = scrollState, flingBehavior = rememberOverscrollFlingBehavior { scrollState }) // must use rememberOverscrollFlingBehavior
    //.horizontalScroll(state = scrollState, flingBehavior = rememberOverscrollFlingBehavior { scrollState }) // must use rememberOverscrollFlingBehavior
) {
    // ...
}
```
### for scrollable Composable like lazyColumn :
```
  val scrollState = rememberLazyListState()
        
        LazyColumn(Modifier
            .fillMaxWidth()
            .overScrollVertical(), // * u should do it
            state = scrollState, // * u should do it 
            flingBehavior = rememberOverscrollFlingBehavior { scrollState } // * u should do it after compose 1.3.x because this's a added param.
        ) {
            // ...
        }
```
### U can call it nested! pls see the demo!

This is an issue submitted to Google about this project:
https://issuetracker.google.com/issues/276682419
https://issuetracker.google.com/issues/261895103
