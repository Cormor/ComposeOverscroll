# ComposeOverscroll
Overscroll any scrollable items!

## Preview

https://user-images.githubusercontent.com/21119517/205872216-905bfd0a-e149-4d48-b61e-0a503b87fb08.mp4


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

