# ComposeOverscroll
Overscroll any scrollable items!

## Preview
### compare with iOS
![overscrollIOS](https://user-images.githubusercontent.com/21119517/206082779-0c98ae17-b54c-4088-bec4-4c2e0cfd5672.gif)

### demo
https://user-images.githubusercontent.com/21119517/205905168-180bc330-85c0-4fbe-a712-8a999e398cb4.mp4


## How to use

### for column :
```
Column(Modifier
        .fillMaxSize()
        .overScrollVertical() // invoke before the scrollable Modifier
        .verticalScroll(rememberScrollState())
        ) { ... }
```
### for scrollable Composable like lazyColumn :
```
LazyColumn(Modifier
            .fillMaxWidth()
            .height(300.dp)
            .overScrollVertical() 
            ) { ... }
```
### U can call it nested! pls see the demo!

