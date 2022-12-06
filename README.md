# ComposeOverscroll
Overscroll any scrollable items!

## Preview


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

