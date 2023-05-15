# OpenBlog
#  OpenBlog项目面试总结

### 项目初始化流程

**问题一：项目的准备工作？**

~~~~java
    设计表 该项目是一个前后端分离的项目 所以先后搭建前端项目 
    初始化后端项目 自定义需求 分析流程入参 响应
项目开始前先统一响应枚举 字面值处理 提高程序的可读性与可维护性 为了方便前后端联调项目中使用了swagger2作为AiP接口测试工具 也有用到PostMan  

        使用全局异常处理统一异常信息 GlobalExceptionHandler
   	    数据库使用MP
    全程使用 MP工具封装的Lambda表达式 加函数式编程以stream()的形式 实现主要的业务代码逻辑  多次涉及到Bean拷贝 所以utils下封装成工具类 前端使用VO封装字段  
 
 swagger2  @ApiModel用于描述实体类。
 实体的属性的描述配置@ApiModelProperty
       
 Article 属性相同字段(名字和类型一致) 涉及 bean 拷贝  HotArticleV0
~~~~

**问题2：如何解决跨域问题?**

~~~~java
首先跨域根本原因是由同源策略引起的（域名，协议，端口相同） 解决跨域方式有很多途径 JSONP  CORS 这里只是做了一个简单的配置重写WebMvcConfigurer（全局跨域） 使用注解@CrossOrigin（局部跨域） WebConfig 类去继承 WebMvcConfigurer 重写addCorsMappings 方法 当然前端axios 中也可以做响应的处理
~~~~

**问题3：如何实现Bean拷贝工具类封装**  

~~~~java
    使用泛型优化
    public static <V> V copyBean(Object source,Class<V> clazz) {
        //创建目标对象
        V result = null;
            //实现属性copy
            BeanUtils.copyProperties(source, result);
        //捕获异常返回结果
        return result;
    public static <O,V> List<V> copyBeanList(List<O> list,Class<V> clazz){
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
~~~~

```
AOP实现日志记录:
 需要通过日志记录接口调用信息。便于后期调试排查。并且可能有很多接口都需要进行日志的记录 相当于是对原有的功能进行增强。并且是批量的增强，这个时候就非常适合用AOP来进行实现。
```

### (一)登录与注册

```
问题3：如何实现用户 的登录与注册、系统 的权限管理

首先前台和后台的认证授权统一都使用SpringSecurity安全框架来实现。 SpringSecurity由一些列的过滤链组成 这里使用核心的功能组件UsernamePasswordAuthenticationFilter：(认证)
FilterSecutityInterceptor: (授权)负责权限校验的过滤器

登录代码的实现: 请求方式Post 根据请求体来设计 
{
    "userName":"sg",
    "password":"1234"
}
controller 以json的形式返回@RequestBody
整体分析流程:
①自定义登录接口  
​调用ProviderManager的方法进行认证 如果认证通过生成jwt  把用户信息存入redis中
​②自定义UserDetailsService 
​在这个实现类中去查询数据库 配置passwordEncoder 为BCryptPasswordEncoder 
校验：
​①定义Jwt认证过滤器
​获取token解析token获取其中的userid 从redis中获取用户信息   存入SecurityContextHolder
```

BlogLoginServiceImpl 

~~~~java
        //通过UsernamePasswordAuthenticationToken 得到 authenticationToken对象 传入UserName,Password
        //判断是否认证通过 如果不通过传入自定义异常
        //封装 LoginUser 获取userid 生成token 封装JwtUtil 通过userid 生成jwt
        //把用户信息存入redis redisCache.setCacheObject("bloglogin:"+userId,loginUser)
        //把token和userinfo封装 返回
        //把User转换成UserInfoVo
~~~~

自定义UserDetailServiceImpl  重写loadUserByUsername方法

~~~~java
        //根据用户名查询用户信息
        //判断是否查到用户  如果没查到抛出异常
        //返回用户信息
        // TODO 查询权限信息封装
~~~~

LoginUser 重写 UserDetails 重写User 成员变量

```
登录校验过滤器代码实现
校验逻辑：
​	①定义Jwt认证过滤器
​				获取token
​				解析token获取其中的userid
​				从redis中获取用户信息
​				存入SecurityContextHolder
```

##### JwtAuthenticationTokenFilter

~~~~java
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取请求头中的token
      //token是否存在token为空说明该接口不需要登录  直接放行
      //解析获取userid
      //token超时  token非法
      //响应告诉前端需要重新登录
      //从redis中获取用户信息
        LoginUser loginUser = redisCache.getCacheObject("bloglogin:" + userId);
        //如果获取不到说明登录过期  提示重新登录
        //存入SecurityContextHolder
        filterChain.doFilter(request, response);
~~~~

认证授权失败处理

```
	目前我们的项目在认证出错或者权限不足的时候响应回来的Json是Security的异常处理结果。但是这个响应的格式肯定是不符合我们项目的接口规范的。所以需要自定义异常处理。
​	AuthenticationEntryPoint 认证失败处理器
​	AccessDeniedHandler 授权失败处理器

主要是异常的实例 响应的结果非空判断authException instanceof BadCredentialsException

最终注册在Config类中 注册异常处理器
```

```
退出登录  Post请求
        //获取token 解析获取userid
        //获取userid
        //删除redis中的用户信息
```

```
用户注册:
请求方式POST  不需要token请求头
自定义需求：密码必须密文存储到数据库中 要求用户名，昵称，邮箱不能和数据库中原有的数据重复 做失败枚举提醒 一些属性的非空判断

代码逻辑:
		//对数据进行非空判断
        //对数据进行是否存在的判断
        //对密码进行加密
		passwordEncoder.encode(user.getPassword());
        //存入数据库
        save(user);
        //返回响应
```



### (二)权限管理

**功能一**

后台权限控制及动态路由

   功能设计使用 [RBAC权限模型] 5 张表

getInfo接口

| 请求方式 | 请求地址 | 请求头          |
| -------- | -------- | --------------- |
| GET      | /getInfo | 需要token请求头 |

响应格式:

如果用户id为1代表管理员，roles 中只需要有admin，permissions中需要有所有菜单类型为C或者F的，状态为正常的，未被删除的权限

~~~~json
{
	"code":200,
	"data":{
		"permissions":[
			"system:user:list",
            "system:role:list",
			"system:menu:list",
			"system:user:query",
			"system:user:add"
            //此次省略1000字
		],
		"roles":[
			"admin"
		],
		"user":{
			"avatar":"http://r7yxkqloa.bkt.clouddn.com/2022/03/05/75fd15587811443a9a9a771f24da458d.png",
			"email":"23412332@qq.com",
			"id":1,
			"nickName":"sg3334",
			"sex":"1"
		}
	},
	"msg":"操作成功"
}
~~~~

##### getRouters接口

| 请求方式 | 请求地址    | 请求头          |
| -------- | ----------- | --------------- |
| GET      | /getRouters | 需要token请求头 |

请求参数： 无

响应格式:

​	前端为了实现动态路由的效果，需要后端有接口能返回用户所能访问的菜单数据。

​	注意：**返回的菜单数据需要体现父子菜单的层级关系**

​	如果用户id为1代表管理员，menus中需要有所有菜单类型为C或者M的，状态为正常的，未被删除的权限

##### getInfo接口

```java
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserInfoVo {

    private List<String> permissions;

    private List<String> roles;

    private UserInfoVo user;
}
```



~~~~java
    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody User user){
        //获取user.getUserName 做非空判断
        //提示 必须要传用户名
    }

    @GetMapping("getInfo")
    public ResponseResult<AdminUserInfoVo> getInfo(){
        //获取当前登录的用户
        //根据用户id查询权限信息
        //根据用户id查询角色信息
        List<String> roleKeyList = roleService.selectRoleKeyByUserId(loginUser.getUser().getId());
        //获取用户信息
        User user = loginUser.getUser();
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        //封装数据返回
        AdminUserInfoVo adminUserInfoVo = new AdminUserInfoVo(perms,roleKeyList,userInfoVo);
        return ResponseResult.okResult(adminUserInfoVo);
    }
~~~~

RoleServiceImpl  selectRoleKeyByUserId方法

~~~~java
@Service("menuService")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Override
    public List<String> selectPermsByUserId(Long id) {
        //如果id == 1L 是管理员，返回所有的权限
        //否则返回selectPermsByUserId 查到的所具有的权限
} 
~~~~

做联表查询

~~~~xml
<mapper namespace="com.sangeng.mapper.MenuMapper">
    <select id="selectPermsByUserId" resultType="java.lang.String">
        SELECT
            DISTINCT m.perms
        FROM
            `sys_user_role` ur
            LEFT JOIN `sys_role_menu` rm ON ur.`role_id` = rm.`role_id`
            LEFT JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
        WHERE
            ur.`user_id` = #{userId} AND
            m.`menu_type` IN ('C','F') AND
            m.`status` = 0 AND
            m.`del_flag` = 0
    </select>
</mapper>
~~~~

MenuServiceImpl  selectPermsByUserId方法

~~~~java
        //判断是否是管理员 如果是返回集合中只需要有admin
        if(id == 1L){
            List<String> roleKeys = new ArrayList<>();
            roleKeys.add("admin");
            return roleKeys;
        }
        //否则查询用户所具有的角色信息
        return getBaseMapper().selectRoleKeyByUserId(id);
~~~~

做联表查询

~~~~xml
<mapper namespace="com.sangeng.mapper.RoleMapper">
    <select id="selectRoleKeyByUserId" resultType="java.lang.String">
        SELECT
            r.`role_key`
        FROM
            `sys_user_role` ur
            LEFT JOIN `sys_role` r ON ur.`role_id` = r.`id`
        WHERE
            ur.`user_id` = #{userId} AND
            r.`status` = 0 AND
            r.`del_flag` = 0
    </select>
</mapper>
~~~~



##### getRouters接口（重点）

RoutersVo

~~~~java
   private List<Menu> menus;
~~~~

LoginController

~~~~java
    @GetMapping("getRouters")
    public ResponseResult<RoutersVo> getRouters(){
        Long userId = SecurityUtils.getUserId();
        //查询menu 结果是tree的形式
        List<Menu> menus = menuService.selectRouterMenuTreeByUserId(userId);
        //封装数据返回
        return ResponseResult.okResult(new RoutersVo(menus));
    }
~~~~

**Impl**

~~~~java
@Override
    public List<Menu> selectRouterMenuTreeByUserId(Long userId) {
        MenuMapper menuMapper = getBaseMapper();
        List<Menu> menus = null;
        //判断是否是管理员
        if(SecurityUtils.isAdmin()){
            //如果是管理员获取所有符合要求的Menu
            menus = menuMapper.selectAllRouterMenu();
        }else{
            //否则获取当前用户所具有的Menu
            menus = menuMapper.selectRouterMenuTreeByUserId(userId);
        }

        //构建tree
        //先找出第一层的菜单  然后去找他们的子菜单设置到children属性中
        List<Menu> menuTree = builderMenuTree(menus,0L);
        return menuTree;
    }

    private List<Menu> builderMenuTree(List<Menu> menus, Long parentId) {
        List<Menu> menuTree = menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .map(menu -> //Menu 里加@Accessors允许链式访问  返回当前对象本身 否则set方法默认没有返回值
                     menu.setChildren(getChildren(menu, menus)))
                .collect(Collectors.toList());
        return menuTree;
    }

    /**
     * 获取存入参数的 子Menu集合
     * @param menu
     * @param menus
     * @return
     */
    private List<Menu> getChildren(Menu menu, List<Menu> menus) {
        List<Menu> childrenList = menus.stream()
                .filter(m -> m.getParentId().equals(menu.getId()))
                .map(m->m.setChildren(getChildren(m,menus)))
                .collect(Collectors.toList());
        return childrenList;
    }
~~~~

MenuMapper.xml

~~~~xml
 <select id="selectAllRouterMenu" resultType="com.sangeng.domain.entity.Menu">
        SELECT
          DISTINCT m.id, m.parent_id, m.menu_name, m.path, m.component, m.visible, m.status, IFNULL(m.perms,'') AS perms, m.is_frame,  m.menu_type, m.icon, m.order_num, m.create_time
        FROM
            `sys_menu` m
        WHERE
            m.`menu_type` IN ('C','M') AND
            m.`status` = 0 AND
            m.`del_flag` = 0
        ORDER BY
            m.parent_id,m.order_num
    </select>
    <select id="selectRouterMenuTreeByUserId" resultType="com.sangeng.domain.entity.Menu">
        SELECT
          DISTINCT m.id, m.parent_id, m.menu_name, m.path, m.component, m.visible, m.status, IFNULL(m.perms,'') AS perms, m.is_frame,  m.menu_type, m.icon, m.order_num, m.create_time
        FROM
            `sys_user_role` ur
            LEFT JOIN `sys_role_menu` rm ON ur.`role_id` = rm.`role_id`
            LEFT JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
        WHERE
            ur.`user_id` = #{userId} AND
            m.`menu_type` IN ('C','M') AND
            m.`status` = 0 AND
            m.`del_flag` = 0
        ORDER BY
            m.parent_id,m.order_num
    </select>
~~~~



**功能二**

```
需求:
	需要对导出分类的接口做权限控制
	
```

SecurityConfig

~~~~java
@EnableGlobalMethodSecurity(prePostEnabled = true)
~~~~

UserDetailsServiceImpl 

~~~~java
在原有的loadUserByUsername中  //判断  user.getType() 是否是ADMAIN
     //根据用户名查询用户信息    
      userMapper.selectOne(queryWrapper);
     //判断是否查到用户  如果没查到抛出异常
     //返回用户信息
     //判断  user.getType() 是否是ADMAIN
     if(user.getType().equals(SystemConstants.ADMAIN)
    //如果是以List形式返回      
    List<String> list = menuMapper.selectPermsByUserId(user.getId());
            return new LoginUser(user,list)
~~~~

LoginUser 增加属性

~~~~java
private List<String> permissions;
~~~~

自定义权限流程 PermissionService  数据库中有管理员相对应的**permission字段**

~~~~java
@Service("ps")
    /**
     * 判断当前用户是否具有permission
     * @param permission 要判断的权限
     * @return
     */
    public boolean hasPermission(String permission){
        //如果是超级管理员  直接返回true
        if(SecurityUtils.isAdmin()){
            return true;
        }
        //否则  获取当前登录用户所具有的权限列表 如何判断是否存在permission
        List<String> permissions = SecurityUtils.getLoginUser().getPermissions();
        return permissions.contains(permission);
~~~~

导出Excel只有管理员具备

~~~~java
@PreAuthorize("@ps.hasPermission('content:category:export')")
~~~~

#####  导出所有分类到Excel

```
需求:
在分类管理中点击导出按钮可以把所有的分类导出到Excel文件中
	使用EasyExcel实现Excel的导出操作
```

| 请求方式 | 请求地址                 | 请求头          |
| -------- | ------------------------ | --------------- |
| GET      | /content/category/export | 需要token请求头 |

根据技术文档   创建WebUtils 设置 setDownLoadHeader ContentType CharacterEncoding setHeader

CategoryController

~~~~java
           //设置下载文件的请求头
            //获取需要导出的数据
            //把数据写入到Excel中
EasyExcel.write(response.getOutputStream(), ExcelCategoryVo.class).autoCloseStream(Boolean.FALSE).sheet("分类导出")
                    .doWrite(excelCategoryVos);
            //如果出现异常也要响应json
            WebUtils.renderString(response, JSON.toJSONString(result))
        
~~~~



### (三)首页信息显示

##### 1.（博客查阅）文章详情接口

需求

​	要求在文章列表点击阅读全文时能够跳转到文章详情页面，可以让用户阅读文章正文。

​	要求：①要在文章详情中展示其分类名

| 请求方式 | 请求路径      |
| -------- | ------------- |
| Get      | /article/{id} |

ArticleController中新增

~~~~java
根据路径匹配  @PathVariable 
@GetMapping("/{id}")  getArticleDetail (@PathVariable("id") Long id){

~~~~

伪代码：

~~~~java
//根据id查询文章  MP  getById
Article article = getById(id);
//转换成VO
        ArticleDetailVo articleDetailVo = BeanCopyUtils.copyBean(article, ArticleDetailVo.class);
//根据分类id查询分类名
        Long categoryId = articleDetailVo.getCategoryId();
        Category category = categoryService.getById(categoryId);
//做非空判断 防止出现空指针      
if(category!=null){
            articleDetailVo.setCategoryName(category.getName());
        }
//封装响应返回
~~~~

##### 2.新发表博客(涉及1文章详情)

分页查询文章列表

```
 需求:
​	在首页和分类页面都需要查询文章列表。
​	首页：查询所有的文章
​	分类页面：查询对应分类下的文章
​	要求：①只能查询正式发布的文章 ②置顶的文章要显示在最前面 

MP支持分页 配置MbatisPlusConfig 

在ArticleController中  GET操作 传入pageNum, pageSize，categoryId  返回articleService.articleList 
```

~~~~java
@Autowired CategoryService 
//构造 LambdaQueryWrapper查询条件
// 如果 有categoryId 就要 查询时要和传入的相同
lambdaQueryWrapper.eq(Objects.nonNull(categoryId)&&categoryId>0 ,Article::getCategoryId,categoryId);
// 状态是正式发布的
Article::getStatus
// 对isTop进行降序
orderByDesc(Article::getIsTop);
//分页查询
//查询categoryName
        articles.stream()
                .map(article -> article.setCategoryName(categoryService.getById(article.getCategoryId()).getName()))
                .collect(Collectors.toList());
        //bean拷贝 封装查询结果
~~~~

PageVo

~~~~java
    private List rows;
    private Long total;
~~~~

在Article中增加一个字段

~~~~java
    @TableField(exist = false)
    private String categoryName
~~~~

##### 3.热门博客

~~~~java
自定义需求:
浏览量最高的前10篇文章 展示文章标题和浏览量 用户自己点击跳转到具体的文章详情  注意：不能把草稿展示出来，不能把删除了的文章查询出来。要按照浏览量进行降序排序。
实现

controller自定义hotArticleList()
service中实现业务逻辑:
        //查询热门文章
        构造 LambdaQueryWrapper
        //必须是正式文章
        queryWrapper.eq
        //按照浏览量进行排序
        queryWrapper.orderByDesc
        //最多只查询10条
        Page<Article> page = new Page(1,10);
        page(page,queryWrapper);
        List<Article> articles = page.getRecords();
        //bean拷贝
        //封装成ResponseResult返回
~~~~

##### 4.博客列表（做分类查询）

 需求

```
	页面上需要展示分类列表，用户可以点击具体的分类查看该分类下的文章列表。
​	注意： ①要求只展示有发布正式文章的分类 ②必须是正常状态的分类
```



![image-20220202111056036](img/image-20220202111056036-16437714601701.png)

涉及两张表查询：

这里使用单表查询  非联表查询

##### 以下全为伪代码思路拆解

~~~~java
注入 @Autowired ArticleService
    //查询文章表  状态为已发布的文章
    LambdaQueryWrapper .eq 传入ARTICLE_STATUS_NORMAL  	进行判断是否已存在
    返回List<Article> 
 	//获取文章的分类id，并且去重
    Set<Long> categoryIds = 			     		     articleList.stream().map(article -> a		 rticle.getCategoryId()).collect(Collectors.toSet());
    //查询分类表
    List<Category> categories = listByIds(categoryIds);
	//过滤 stream().filter 拿到category是正常状态 转成Collectors.toList()
    //封装vo 返回响应数据
~~~~



### (四)博客与友联

#### 写博文

##### 1.发表编辑博客 

```
需求
需要提供写博文的功能，写博文时需要关联分类和标签。

​	可以上传缩略图，也可以在正文中添加图片。

​	文章可以直接发布，也可以保存到草稿箱。

```

##### 分别实现

#####  查询所有分类接口

| 请求方式 | 请求地址                          | 请求头          |
| -------- | --------------------------------- | --------------- |
| GET      | /content/category/listAllCategory | 需要token请求头 |

##### 查询所有标签接口

| 请求方式 | 请求地址                | 请求头          |
| -------- | ----------------------- | --------------- |
| GET      | /content/tag/listAllTag | 需要token请求头 |

##### 上传图片

| 请求方式 | 请求地址 | 请求头          |
| -------- | -------- | --------------- |
| POST     | /upload  | 需要token请求头 |

##### 新增博文

| 请求方式 | 请求地址         | 请求头          |
| -------- | ---------------- | --------------- |
| POST     | /content/article | 需要token请求头 |

##### 查询所有分类接口

CategoryVo修改,增加description属性  CategoryService增加listAllCategory方法

SystemConstants中增加常量

~~~~java
    /** 正常状态 */
    public static final String NORMAL = "0";
~~~~

CategoryServiceImpl增加方法

~~~~java
       wrapper.eq(Category::getStatus, SystemConstants.NORMAL);
//判断状态 是否正常
//list查询 bean拷贝 categoryVos返回  
        List<Category> list = list(wrapper);
        List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(list, CategoryVo.class);
~~~~

##### 查询所有标签接口

TagVo  TagController  TagService 增加listAllTag方法

TagServiceImpl

~~~~java
        wrapper.select(Tag::getId,Tag::getName);
        List<Tag> list = list(wrapper);
        List<TagVo> tagVos = BeanCopyUtils.copyBeanList(list, TagVo.class);
        return tagVos;
~~~~

##### 上传图片接口

复用之前代码上传逻辑

在sangeng-admin中增加UploadController  调用前台API

~~~~java
注意入参即可  public ResponseResult uploadImg(@RequestParam("img") MultipartFile multipartFile) 
~~~~

##### 新增博文接口

ArticleController

~~~~java
 articleService.add(article);
~~~~

AddArticleDto

注意增加tags属性用于接收文章关联标签的id

~~~~java
    private List<Long> tags;
~~~~

Article 修改这样创建时间创建人修改时间修改人可以自动填充

~~~~java
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
~~~~

ArticleService增加方法

~~~~java
ResponseResult add(AddArticleDto article);
~~~~

ArticleServiceImpl增加如下代码   难点是如何**关联动态标签**

~~~~java
        //添加 博客
        Article article = BeanCopyUtils.copyBean(articleDto, Article.class);
        save(article);
//从ArticleTag拿到article.getId() 与tagId做比对生成List集合返回
        List<ArticleTag> articleTags = articleDto.getTags().stream()
                .map(tagId -> new ArticleTag(article.getId(), tagId))
                .collect(Collectors.toList());
        //添加 博客和标签的关联
        articleTagService.saveBatch(articleTags);
~~~~

#### 博客评论

1.**查询评论列表接口重点**

![image-20220208214106296](img/image-20220208214106296.png)



 接口设计

| 请求方式 | 请求地址             | 请求头            |
| -------- | -------------------- | ----------------- |
| GET      | /comment/commentList | 不需要token请求头 |

Query格式请求参数： articleId:文章id pageNum: 页码  pageSize: 每页条数

#####  不考虑子评论

CommentController

~~~~java
    @GetMapping("/commentList")
    public ResponseResult commentList(Long articleId,Integer pageNum,Integer pageSize){
        return commentService.commentList(articleId,pageNum,pageSize);
~~~~

CommentServiceImpl

~~~~java
        //查询对应文章的根评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        //对articleId进行判断
    queryWrapper.eq(Comment::getArticleId,articleId);
        //根评论 rootId为-1
        queryWrapper.eq(Comment::getRootId,-1);
        //分页查询
        Page<Comment> page = new Page(pageNum,pageSize);
        page(page,queryWrapper);
        List<CommentVo> commentVoList = toCommentVoList(page.getRecords());
        return ResponseResult.okResult(new PageVo(commentVoList,page.getTotal()));

    private List<CommentVo> toCommentVoList(List<Comment> list){
        List<CommentVo> commentVos = BeanCopyUtils.copyBeanList(list, CommentVo.class);
        //遍历vo集合
        for (CommentVo commentVo : commentVos) {
            //通过creatyBy查询用户的昵称并赋值
            String nickName = userService.getById(commentVo.getCreateBy()).getNickName();
            commentVo.setUsername(nickName);
            //通过toCommentUserId查询用户的昵称并赋值
            //出现空指针
            //如果toCommentUserId不为-1才进行查询
            if(commentVo.getToCommentUserId()!=-1){String toCommentUserName = 			                  userService.getById(commentVo.getToCommentUserId()).getNickName();
                commentVo.setToCommentUserName(toCommentUserName);
            }
        }
        return commentVos;
~~~~

##### 查询子评论

```
CommentVo在之前的基础上增加了  private List<CommentVo> children;
```

CommentServiceImpl

~~~~java
    public ResponseResult commentList(Long articleId, Integer pageNum, Integer pageSize) {
        //查询对应文章的根评论 //根评论 rootId为-1
        //对articleId进行判断
        queryWrapper.eq(Comment::getArticleId,articleId);
        queryWrapper.eq(Comment::getRootId,-1);
        //分页查询
        //查询所有根评论对应的子评论集合，并且赋值给对应的属性
        for (CommentVo commentVo : commentVoList) {
            //查询对应的子评论
            List<CommentVo> children = getChildren(commentVo.getId());
            //赋值
            commentVo.setChildren(children);
        }
        return ResponseResult.okResult(new PageVo(commentVoList,page.getTotal()));
    }

    /**
     * 根据根评论的id查询所对应的子评论的集合
     * @param id 根评论的id  
     * @return
     */
    private List<CommentVo> getChildren(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
		//id 20--->1 
        queryWrapper.eq(Comment::getRootId,id);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> comments = list(queryWrapper);
        List<CommentVo> commentVos = toCommentVoList(comments);
        return commentVos;
    }
}
~~~~

##### 2.发表评论接口

```
用户登录后可以对文章发表评论，也可以对评论进行回复。

​	用户登录后也可以在友链页面进行评论。
```

| 请求方式 | 请求地址 | 请求头      |
| -------- | -------- | ----------- |
| POST     | /comment | 需要token头 |

##### 请求体：

回复了文章：

~~~~json
{"articleId":1,"type":0,"rootId":-1,"toCommentId":-1,"toCommentUserId":-1,"content":"评论了文章"}
~~~~

回复了某条评论：

~~~~json
{"articleId":1,"type":0,"rootId":"3","toCommentId":"3","toCommentUserId":"1","content":"回复了某条评论"}
~~~~

如果是友链评论，type应该为1

响应格式：

~~~~java
{
	"code":200,
	"msg":"操作成功"
}
~~~~

CommentServiceImpl

~~~~java
    @Override
    public ResponseResult addComment(Comment comment) {
        //评论内容不能为空
        if(!StringUtils.hasText(comment.getContent())){
            throw new SystemException(AppHttpCodeEnum.CONTENT_NOT_NULL);
        }
        save(comment);
        return ResponseResult.okResult();
    }
~~~~



配置MP字段自动填充....

用注解标识哪些字段在什么情况下需要自动填充

~~~~java
    /**
     * 创建人的用户id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
~~~~

##### 3.友联评论列

| 请求方式 | 请求地址                 | 请求头            |
| -------- | ------------------------ | ----------------- |
| GET      | /comment/linkCommentList | 不需要token请求头 |

SystemConstants增加了两个常量

~~~~java
    /**
     * 评论类型为：文章评论
     */
    public static final String ARTICLE_COMMENT = "0";
    /**
     * 评论类型为：友联评论
     */
    public static final String LINK_COMMENT = "1"
~~~~

CommentServiceImpl修改commentList方法的代码，必须commentType为0的时候才增加articleId的判断，并且增加了一个评论类型的添加。

~~~~java
 
        //查询对应文章的根评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        //对articleId进行判断
        //常量写在前面 防止空指针
        queryWrapper.eq(SystemConstants.ARTICLE_COMMENT.equals(commentType),Comment::getArticleId,articleId);
        //根评论 rootId为-1
        //评论类型
        queryWrapper.eq(Comment::getType,commentType);
       //分页查询
~~~~

#### 博客点赞

赞赏功能需接入第三方API 待完善

#### 友联相关

查询：

```
 需求

​	在友链页面要查询出所有的审核通过的友链。

```

| 请求方式 | 请求路径         |
| -------- | ---------------- |
| Get      | /link/getAllLink |

Controller

~~~~java
linkService.getAllLink();
~~~~

~~~~java
        //查询所有审核通过的
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Link::getStatus, SystemConstants.LINK_STATUS_NORMAL);
        List<Link> links = list(queryWrapper);
        //转换成vo
        List<LinkVo> linkVos = BeanCopyUtils.copyBeanList(links, LinkVo.class);
        //封装返回
        return ResponseResult.okResult(linkVos);
~~~~

SystemConstants

~~~~java
    /**
     * 友链状态为审核通过
     */
    public static final String  LINK_STATUS_NORMAL = "0";
~~~~



### (五)个人信息展示与修改

##### 1.展示

进入个人中心的时候需要能够查看当前用户信息

| 请求方式 | 请求地址       | 请求头          |
| -------- | -------------- | --------------- |
| GET      | /user/userInfo | 需要token请求头 |

不需要参数

UserServiceImpl实现userInfo方法

~~~~java
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();
        //根据用户id查询用户信息
        User user = getById(userId);
        //封装成UserInfoVo
        UserInfoVo vo = BeanCopyUtils.copyBean(user,UserInfoVo.class);
        return ResponseResult.okResult(vo);
~~~~

##### 2.修改（头像上传）

修改：

在编辑完个人资料后点击保存会对个人资料进行更新。

请求方式PUT  需要token请求头       调用mybatis中的  updateById 方法即可

头像上传：

```
在个人中心点击编辑的时候可以上传头像图片。上传完头像后，可以用于更新个人信息接口。
​OOS使用：借助七牛云
​因为如果把图片视频等文件上传到自己的应用的Web服务器，在读取图片的时候会占用比较多的资源。影响应用服务器的性能。所以我们一般使用OSS(Object Storage Service对象存储服务)存储图片或视频。
oss:
  accessKey: xxxx
  secretKey: xxxx
  bucket: openblog
```

| 请求方式 | 请求地址 | 请求头    |
| -------- | -------- | --------- |
| POST     | /upload  | 需要token |

参数： img,值为要上传的文件

请求头：Content-Type ：multipart/form-data;

响应格式:

~~~~json
{
    "code": 200,
    "data": "文件访问链接",
    "msg": "操作成功"
}
~~~~

~~~~java
    @PostMapping("/upload")
    public ResponseResult uploadImg(MultipartFile img){
        return uploadService.uploadImg(img);
    }
~~~~

~~~~java
 public ResponseResult uploadImg(MultipartFile img) {
        //判断文件类型
        //获取原始文件名
        String originalFilename = img.getOriginalFilename();
        //对原始文件名进行判断
        if(!originalFilename.endsWith(".png"))
        //如果判断通过上传文件到OSS
        String filePath = PathUtils.generateFilePath(originalFilename);
        String url = uploadOss(img,filePath);//  2099/2/3/wqeqeqe.png
        return ResponseResult.okResult(url);
    }

    private String accessKey;
    private String secretKey;
    private String bucket;

    private String uploadOss(MultipartFile imgFile, String filePath){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.autoRegion());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        //解析上传成功的结果
    }
~~~~



### (六)阅读量统计与后台维护功能CRUD

#### 阅读量统计

```
在用户浏览博文时要实现对应博客浏览量的增加。
​	我们只需要在每次用户浏览博客时更新对应的浏览数即可。
​	但是如果直接操作博客表的浏览量的话，在并发量大的情况下会出现什么问题呢？
​	如何去优化呢？

①在应用启动时把博客的浏览量存储到redis中
②更新浏览量时去更新redis中的数据
③每隔10分钟把Redis中的浏览量更新到数据库中
④读取文章浏览量时从redis读取
CommandLineRunner实现项目启动时预处理  定时任务
用SpringBoot提供的原生定时任务工具
① 使用@EnableScheduling注解开启定时任务功能
​	我们可以在配置类上加上@EnableScheduling
② 确定定时任务执行代码，结合cron 表达式 并配置任务执行时间
​	使用@Scheduled注解标识需要定时执行的代码。注解的cron属性相当于是任务的执行时间。目前可以使用 0/5 * * * * ? 进行测试，代表从0秒开始，每隔5秒执行一次。 
​	注意：@Component 对应的bean要注入容器，否则不会生效。
```

| 请求方式 | 请求地址                      | 请求头            |
| -------- | ----------------------------- | ----------------- |
| PUT      | /article/updateViewCount/{id} | 不需要token请求头 |

参数 ：   请求路径中携带文章id

##### ①在应用启动时把博客的浏览量存储到redis中

​	实现CommandLineRunner接口，在应用启动时初始化缓存。

~~~~java
    public void run(String... args) throws Exception {
        //查询博客信息  id  viewCount
        List<Article> articles = articleMapper.selectList(null);
        Map<String, Integer> viewCountMap = articles.stream()
                .collect(Collectors.toMap(article -> article.getId().toString(), article -> {
                    return article.getViewCount().intValue();//
                }));
        //存储到redis中
        redisCache.setCacheMap("article:viewCount",viewCountMap);
    }

~~~~

##### ②更新浏览量时去更新redis中的数据

RedisCache增加方法   

~~~~java
    public void incrementCacheMapValue(String key,String hKey,long v){
        redisTemplate.boundHashOps(key).increment(hKey, v);
    }
~~~~

ArticleController中增加方法更新阅读数

~~~~java
    @PutMapping("/updateViewCount/{id}")
    public ResponseResult updateViewCount(@PathVariable("id") Long id){
        return articleService.updateViewCount(id);
    }
~~~~

ArticleService中增加方法

~~~~java
ResponseResult updateViewCount(Long id);
~~~~

ArticleServiceImpl中实现方法

~~~~java
    @Override
    public ResponseResult updateViewCount(Long id) {
        //更新redis中对应 id的浏览量
        redisCache.incrementCacheMapValue("article:viewCount",id.toString(),1);
        return ResponseResult.okResult();
    }
~~~~

##### ③定时任务每隔10分钟把Redis中的浏览量更新到数据库中

Article中增加构造方法

~~~~java
    public Article(Long id, long viewCount) {
        this.id = id;
        this.viewCount = viewCount;
    }
~~~~

~~~~java
    @Scheduled(cron = "0/5 * * * * ?")
    public void updateViewCount(){
        //获取redis中的浏览量
        Map<String, Integer> viewCountMap = redisCache.getCacheMap("article:viewCount");
        List<Article> articles = viewCountMap.entrySet()
                .stream()
                .map(entry -> new Article(Long.valueOf(entry.getKey()), entry.getValue().longValue()))
                .collect(Collectors.toList());
        //更新到数据库中
        articleService.updateBatchById(articles);
    }
~~~~

##### ④读取文章浏览量时从redis读取

~~~~java
    @Override
    public ResponseResult getArticleDetail(Long id) {
        //根据id查询文章
        Article article = getById(id);
        //从redis中获取viewCount
        Integer viewCount = redisCache.getCacheMapValue("article:viewCount", id.toString());
        article.setViewCount(viewCount.longValue());
        //转换成VO
        ArticleDetailVo articleDetailVo = BeanCopyUtils.copyBean(article, ArticleDetailVo.class);
        //根据分类id查询分类名
        Long categoryId = articleDetailVo.getCategoryId();
        Category category = categoryService.getById(categoryId);
        if(category!=null){
            articleDetailVo.setCategoryName(category.getName());
        }
        //封装响应返回
        return ResponseResult.okResult(articleDetailVo);
    }
~~~~



#### 后台维护功能CRUD

##### 1.博客删除  

点击文章后面的删除按钮可以删除该文章

注意：是逻辑删除不是物理删除

| 请求方式 | 请求路径             | 是否需求token头 |
| -------- | -------------------- | --------------- |
| DELETE   | content/article/{id} | 是              |

Path请求参数：

id：要删除的文章id

```java
@DeleteMapping("/{id}")
public ResponseResult delete(@PathVariable Long id){
    articleService.removeById(id);
    return ResponseResult.okResult();
}
```



##### 2.用户菜单分类管理

用户(角色)

```
@Autowired
private RoleService roleService;


@GetMapping("/listAllRole")
public ResponseResult listAllRole(){
    List<Role> roles = roleService.selectRoleAll();
    return ResponseResult.okResult(roles);
}

/**
 * 根据角色编号获取详细信息
 */
@GetMapping(value = "/{roleId}")
public ResponseResult getInfo(@PathVariable Long roleId)
{
    Role role = roleService.getById(roleId);
    return ResponseResult.okResult(role);
}

/**
 * 修改保存角色
 */
@PutMapping
public ResponseResult edit(@RequestBody Role role)
{
    roleService.updateRole(role);
    return ResponseResult.okResult();
}

/**
 * 删除角色
 * @param id
 */
@DeleteMapping("/{id}")
public ResponseResult remove(@PathVariable(name = "id") Long id) {
    roleService.removeById(id);
    return ResponseResult.okResult();
}


/**
 * 新增角色
 */
@PostMapping
public ResponseResult add( @RequestBody Role role)
{
    roleService.insertRole(role);
    return ResponseResult.okResult();

}
@GetMapping("/list")
public ResponseResult list(Role role, Integer pageNum, Integer pageSize) {
    return roleService.selectRolePage(role,pageNum,pageSize);
}

@PutMapping("/changeStatus")
public ResponseResult changeStatus(@RequestBody ChangeRoleStatusDto roleStatusDto){
    Role role = new Role();
    role.setId(roleStatusDto.getRoleId());
    role.setStatus(roleStatusDto.getStatus());
    return ResponseResult.okResult(roleService.updateById(role));
}
```

菜单

```java
@Autowired
private MenuService menuService;


/**
 * 获取菜单下拉树列表
 */
@GetMapping("/treeselect")
public ResponseResult treeselect() {
    //复用之前的selectMenuList方法。方法需要参数，参数可以用来进行条件查询，而这个方法不需要条件，所以直接new Menu()传入
    List<Menu> menus = menuService.selectMenuList(new Menu());
    List<MenuTreeVo> options =  SystemConverter.buildMenuSelectTree(menus);
    return ResponseResult.okResult(options);
}

/**
 * 加载对应角色菜单列表树
 */
@GetMapping(value = "/roleMenuTreeselect/{roleId}")
public ResponseResult roleMenuTreeSelect(@PathVariable("roleId") Long roleId) {
    List<Menu> menus = menuService.selectMenuList(new Menu());
    List<Long> checkedKeys = menuService.selectMenuListByRoleId(roleId);
    List<MenuTreeVo> menuTreeVos = SystemConverter.buildMenuSelectTree(menus);
    RoleMenuTreeSelectVo vo = new RoleMenuTreeSelectVo(checkedKeys,menuTreeVos);
    return ResponseResult.okResult(vo);
}

/**
 * 获取菜单列表
 */
@GetMapping("/list")
public ResponseResult list(Menu menu) {

List<Menu> menus = menuService.selectMenuList(menu);
List<MenuVo> menuVos = BeanCopyUtils.copyBeanList(menus, MenuVo.class);
    return ResponseResult.okResult(menuVos);
}


@PostMapping
public ResponseResult add(@RequestBody Menu menu)
{
    menuService.save(menu);
    return ResponseResult.okResult();
}

/**
 * 根据菜单编号获取详细信息
 */
@GetMapping(value = "/{menuId}")
public ResponseResult getInfo(@PathVariable Long menuId){
    return ResponseResult.okResult(menuService.getById(menuId));
}

/**
 * 修改菜单
 */
@PutMapping
public ResponseResult edit(@RequestBody Menu menu) {
    if (menu.getId().equals(menu.getParentId())) {
        return ResponseResult.errorResult(500,"修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
    }
    menuService.updateById(menu);
    return ResponseResult.okResult();
}

/**
 * 删除菜单
 */
@DeleteMapping("/{menuId}")
public ResponseResult remove(@PathVariable("menuId") Long menuId) {
    if (menuService.hasChild(menuId)) {
        return ResponseResult.errorResult(500,"存在子菜单不允许删除");
    }
    menuService.removeById(menuId);
    return ResponseResult.okResult();
}
```

分类

```java
@Autowired
private CategoryService categoryService;

@GetMapping("/listAllCategory")
public ResponseResult listAllCategory(){
    List<CategoryVo> list = categoryService.listAllCategory();
    return ResponseResult.okResult(list);
}

@PutMapping
public ResponseResult edit(@RequestBody Category category){
    categoryService.updateById(category);
    return ResponseResult.okResult();
}

@DeleteMapping(value = "/{id}")
public ResponseResult remove(@PathVariable(value = "id")Long id){
    categoryService.removeById(id);
    return ResponseResult.okResult();
}


@GetMapping(value = "/{id}")
public ResponseResult getInfo(@PathVariable(value = "id")Long id){
    Category category = categoryService.getById(id);
    return ResponseResult.okResult(category);
}
@PostMapping
public ResponseResult add(@RequestBody Category category){
    categoryService.save(category);
    return ResponseResult.okResult();
}

@GetMapping("/list")
public ResponseResult list(Category category, Integer pageNum, Integer pageSize) {
    PageVo pageVo = categoryService.selectCategoryPage(category,pageNum,pageSize);
    return ResponseResult.okResult(pageVo);
}

@PreAuthorize("@ps.hasPermission('content:category:export')")
@GetMapping("/export")
public void export(HttpServletResponse response){
    try {
        //设置下载文件的请求头
        WebUtils.setDownLoadHeader("分类.xlsx",response);
        //获取需要导出的数据
        List<Category> categoryVos = categoryService.list();

        List<ExcelCategoryVo> excelCategoryVos = BeanCopyUtils.copyBeanList(categoryVos, ExcelCategoryVo.class);
        //把数据写入到Excel中
        EasyExcel.write(response.getOutputStream(), ExcelCategoryVo.class).autoCloseStream(Boolean.FALSE).sheet("分类导出")
                .doWrite(excelCategoryVos);

    } catch (Exception e) {
        //如果出现异常也要响应json
        ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR);
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
```

友联

```
@Autowired
private LinkService linkService;

@GetMapping("/list")
public ResponseResult list(Link link, Integer pageNum, Integer pageSize)
{
    PageVo pageVo = linkService.selectLinkPage(link,pageNum,pageSize);
    return ResponseResult.okResult(pageVo);
}

@PostMapping
public ResponseResult add(@RequestBody Link link){
    linkService.save(link);
    return ResponseResult.okResult();
}

@DeleteMapping("/{id}")
public ResponseResult delete(@PathVariable Long id){
    linkService.removeById(id);
    return ResponseResult.okResult();
}

@PutMapping
public ResponseResult edit(@RequestBody Link link){
    linkService.updateById(link);
    return ResponseResult.okResult();
}

@PutMapping("/changeLinkStatus")
public ResponseResult changeLinkStatus(@RequestBody Link link){
    linkService.updateById(link);
    return ResponseResult.okResult();
}

@GetMapping(value = "/{id}")
public ResponseResult getInfo(@PathVariable(value = "id")Long id){
    Link link = linkService.getById(id);
    return ResponseResult.okResult(link);
}
```



#### 标签管理

分页查询:    增删改逻辑复用

 ```
  需求：
为了方便后期对文章进行管理，需要提供标签的功能，一个文章可以有多个标签。
​    在后台需要分页查询标签功能，要求能根据标签名进行分页查询。 **后期可能会增加备注查询等需求**。
​	注意：不能把删除了的标签查询出来。
 ```

| 请求方式 | 请求路径         |
| -------- | ---------------- |
| Get      | content/tag/list |

响应格式：

~~~~json
{
	"code":200,
	"data":{
		"rows":[
			{
				"id":4,
				"name":"Java",
				"remark":"sdad"
			}
		],
		"total":1
	},
	"msg":"操作成功"
}
~~~~

~~~~java
@RestController
@RequestMapping("/content/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public ResponseResult<PageVo> list(Integer pageNum, Integer pageSize, TagListDto tagListDto){
        return tagService.pageTagList(pageNum,pageSize,tagListDto);
    }
}
~~~~

两个dto

~~~~java
@Service("tagService")
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public ResponseResult<PageVo> pageTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto) {
        //分页查询
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(tagListDto.getName()),Tag::getName,tagListDto.getName());
        queryWrapper.eq(StringUtils.hasText(tagListDto.getRemark()),Tag::getRemark,tagListDto.getRemark());

        Page<Tag> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page(page, queryWrapper);
        //封装数据返回
        PageVo pageVo = new PageVo(page.getRecords(),page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}
~~~~

#### 维护博客系统

##### 1.分页查询

```
文章列表:
​	为了对文章进行管理，需要提供文章列表，
​    在后台需要分页查询文章功能，要求能根据标题和摘要**模糊查询**。 
​	注意：不能把删除了的文章查询出来
```

| 请求方式 | 请求路径              | 是否需求token头 |
| -------- | --------------------- | --------------- |
| Get      | /content/article/list | 是              |

Query格式请求参数： pageNum: 页码 pageSize: 每页条数  title：文章标题   summary：文章摘要

```java
@Override
public PageVo selectArticlePage(Article article, Integer pageNum, Integer pageSize) {
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper();

    queryWrapper.like(StringUtils.hasText(article.getTitle()),Article::getTitle, article.getTitle());
    queryWrapper.like(StringUtils.hasText(article.getSummary()),Article::getSummary, article.getSummary());

    Page<Article> page = new Page<>();
    page.setCurrent(pageNum);
    page.setSize(pageSize);
    page(page,queryWrapper);

    //转换成VO
    List<Article> articles = page.getRecords();

    //这里偷懒没写VO的转换 应该转换完在设置到最后的pageVo中

    PageVo pageVo = new PageVo();
    pageVo.setTotal(page.getTotal());
    pageVo.setRows(articles);
    return pageVo;
}
```

##### 2.修改文章

​	点击文章列表中的修改按钮可以跳转到写博文页面。回显示该文章的具体信息。

​	用户可以在该页面修改文章信息。点击更新按钮后修改文章

​	这个功能的实现首先需要能够根据文章id查询文章的详细信息这样才能实现文章的回显。

​	如何需要提供更新文章的接口。

| 请求方式 | 请求路径             | 是否需求token头 |
| -------- | -------------------- | --------------- |
| Get      | content/article/{id} | 是              |

Path格式请求参数：

id: 文章id

```java
@Override
public ArticleVo getInfo(Long id) {
    Article article = getById(id);
    //获取关联标签
    LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
    articleTagLambdaQueryWrapper.eq(ArticleTag::getArticleId,article.getId());
    List<ArticleTag> articleTags = articleTagService.list(articleTagLambdaQueryWrapper);
    List<Long> tags = articleTags.stream().map(articleTag -> articleTag.getTagId()).collect(Collectors.toList());
    ArticleVo articleVo = BeanCopyUtils.copyBean(article,ArticleVo.class);
    articleVo.setTags(tags);
    return articleVo;
}
```

| 请求方式 | 请求路径        | 是否需求token头 |
| -------- | --------------- | --------------- |
| PUT      | content/article | 是              |

```java
@Override
public void edit(ArticleDto articleDto) {
    Article article = BeanCopyUtils.copyBean(articleDto, Article.class);
    //更新博客信息
    updateById(article);
    //删除原有的 标签和博客的关联
    LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
    articleTagLambdaQueryWrapper.eq(ArticleTag::getArticleId,article.getId());
    articleTagService.remove(articleTagLambdaQueryWrapper);
    //添加新的博客和标签的关联信息
    List<ArticleTag> articleTags = articleDto.getTags().stream()
            .map(tagId -> new ArticleTag(articleDto.getId(), tagId))
            .collect(Collectors.toList());
    articleTagService.saveBatch(articleTags);

}
```
