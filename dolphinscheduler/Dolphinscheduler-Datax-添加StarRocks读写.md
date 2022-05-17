# Dolphinscheduler-Datax 添加StarRocks读写

## 前台界面创建数据源中添加 StarRocks 选项



### 添加loadUrl 选项，并且是必填

```properties
"loadUrl": ["10.166.147.62:38100", "10.166.147.63:38100"]
```

```xml
<m-list-box-f v-if="showLoadUrls">
    <template slot="name"><strong>*</strong>{{$t('StarRocks loadUrl')}}</template>
    <template slot="content">
        <el-input
                  type="textarea"
                  v-model="loadUrl"
                  :autosize="{minRows:2}"
                  size="small"
                  :placeholder="$t('Place enter loadUrl')">
        </el-input>
    </template>
</m-list-box-f>
```



```js
# 添加
// loadUrl
loadUrl: '',
showLoadUrls: false,

    # watch 模块中添加
    
    if (value === 'STARROCKS' && !this.item.id) {
        this.showLoadUrls = true
        this.connectType = 'STARROCKS_SERVICE_NAME'
    } else if (value === 'STARROCKS' && this.item.id) {
        this.showLoadUrls = true
    } else {
        this.showLoadUrls = false
    }
```

