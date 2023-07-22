
## 单点登录

单点登录希望能够保存住「已经登录」的状态，使得用户在一定时间内能够直接访问已经登录后的网站而无需再次登录。

HTTP协议是一种无状态的协议，因此为了实现单点登录，还需要一些额外方法。

### Cookie

用户登录之后，由服务器生成用户认证信息保存到Cookie当中，返回给客户端。客户端在请求访问其他系统时，可以通过验证 Cookie 中的信息，实现单点登录。

Cookie 实现单点登录的特点可以从存储、运输两个方面考虑：

1. 存储方式：单点登录所需要的用户信息存储在 Cookie 中
2. 存储大小：Cookie 在浏览器中的存储有大小限制，4KB，只能存储有限的用户信息
3. 传输方式：用户信息直接存在 Token 中，跟 HTTP 请求一起带过去。
4. 传输安全性：Cookie 是不安全的，例如 Cookie 很容易受到**跨站请求伪造 CSRF** 攻击
5. 跨域传输：Cookie 受同源策略的限制，也就是说，只有在同源（协议、域名、端口都相同）的服务当中使用，否则需要重新配置跨域策略。

> 什么是跨站请求伪造？
> 
> 由于我们提到了 Cookie 是跟着请求自动发送的，这意味着黑客可以在自己的网站放一个恶意链接（如一个银行转账请求），用户误点此伪造的请求后，用户认证信息通过 Cookie 直接到达银行网站，通过校验，实现了恶意攻击。
> 
> 可以看到，CSRF 攻击的根本原因在于，黑客不需要感知到 Cookie 的认证信息即可伪造一个请求。

### Token

Token 认证可以看成对 Cookie 的改进。在 Cookie 认证当中，用户的认证信息存放在 Cookie，而 Token 认证是服务端依据用户认证信息生成一个加密的字符串（Token），显式地交还给客户端。

同样的，在请求认证时，用户需要显式地在请求中带上 Token，由服务端完成验证的过程。服务端验证可以有以下两种策略：

1. 基于加密解密的认证（JWT）：服务端拿到加密后的 Token，进行解密（对称/非对称），还原用户认证信息（用户id、过期时间等）从而确认用户是否已经登录。对于分布式的多系统来说，多系统需要约定好相同的解密方法。
2. 基于 Redis 的认证：在 Redis 中存放 Token - 认证信息 的 pair，服务端拿到 Token 后，请求另外的 Redis 服务，看此 token key 是否存在/过期，并通过 key 获取对应的认证信息。对于多系统，确保连接到同一个 Redis 服务即可。

两种认证策略比较：

1. 时耗：加解密是比较耗时的（尤其是非对称加密），而 Redis 可以用更简单的 token（无需加密），且查一次很快
2. 安全性：加解密虽然安全，但是一旦密钥泄露，系统就寄了。Redis 依赖的是局域内的 Redis 服务，更安全。
3. 维护性：加解密的多系统认证依赖于共识密钥，一旦密钥变动就不好维护了。Redis 没有此问题。
4. 稳定性：加解密认证只依赖服务端本身，而 Redis 认证依赖 Redis，一旦 Redis 寄了，整个认证服务就寄了。

> 为什么非对称加密比对称加密更慢？
> 
> 主要在于计算复杂度上，非对称加密算法（RSA、ECC）等通常基于一些难解的数学问题，如大数因子分解、离散对数问题，并且有大数的乘除模法，这些问题涉及的复杂度很高，因此计算比较慢。
> 而对称加密的计算少（AES-128只有10轮），计算简单（都是位运算），因此计算很快。

> RSA 加密的过程是怎么样的？
> 1. 密钥生成：选取两个大质数 p，q。生成 n = p * q。再生成 e，要求 e \< phi(n)，且与之互质。再生成 d，要求 e * d = 1 mod phi(n). 于是 pub = (n, e), prv = (n, d)。
> 2. 加密：C = M^e mod n
> 3. 解密：M = C^d mod n

Token 特点及与 Cookie 比较：

基本上来说，Token 解决了 Cookie 的缺点。还是从存储、运输的角度的考虑：

1. 存储方式：加密的用户信息本身，或者是用户信息的 key 存储在 Token 中。
2. 存储大小：Token 没有大小的限制。而 Cookie 最多 4KB。
3. 传输方式：Token 需要显式地写在请求体当中。
4. 传输安全性：Token 可以避免 CSRF 攻击，因此黑客无法获得 Token。而 Cookie 更容易被攻击。
5. 传输域：Token 不受同源策略的限制，可以跨域传输。而 Cookie 需要配置传输策略。

#### 双token登录

双 Token 登录在**安全**和**体验**之间取了一个平衡。

单 Token（访问Token）为了实现安全性，会设置比较短的过期时间。这意味着用户需要频繁地登录获取新的访问 Token，降低了用户的体验。如果延长过期时间，那么访问令牌泄露的后果又很严重。

双 Token 使用两个 Token，一个是访问 Token（Access Token, AT）, 一个是刷新 Token（Refresh Token, RT）。

访问 Token 的过期时间很短（30 min），而刷新 Token 的过期时间比较长（7 days）。

用户通过访问 token 和刷新 token 进行认证。如果访问 token 过期了，可以通过 过期访问token + 刷新 token 申请到新的访问 token。

双 token 的特点：

1. 安全性：尽管安全性不如短AT，比长AT更安全。因为生成新的过期信息需要 过期AT + RT，两个同时泄露才行。
2. 体验：使用 RT 之后，登录周期大大延长，提高了用户的体验。

### session

Session 也可以实现和 Token 类似的认证功能。

但是相比于 Token，也有以下的不足：

1. 同源策略
2. 状态：session 是有状态的，服务器需要维护相关的状态（ID、用户信息等）
3. 安全性：session 也会受到 CSRF 攻击。

> Cookie, Session, Token 有什么区别？
> 
> - Cookie 是存储的客户端的小型文件，用于保存用户的状态信息。
> - Session 是存储在服务端的数据，也用于保存用户的状态信息。
> - Token 是用于身份认证和授权的加密字符串。
>
> 存储方式：Cookie 在客户端，Session 服务端，Token 在客户端
> 存储大小：Cookie 4KB, Session Token 无限制
> 传输范围：Cookie, Session 同源策略，Token 无限制
> 传输安全性：Cookie, Session 都不安全易受 CSRF 攻击，而 Token 更安全。
> 传输方式：Cookie 和 Session 都是自动传递在请求当中的，而 Token 需要显式地在请求体当中指定。

## 分页查询

分页查询是一个很基本的查询需求，主要包括了两个参数：pageIndex, pageSize。其中 pageIndex with pageSize -> offset。通过 offset + pageSize, 可以得到 offset 之后 pageSize 条记录。

### MySQL 实现

常见的实现是 `order by xxx limit os, ps`。

limit 在实际上是**基于排序**实现的，因此需要使用 order by, 当然如果不用 order by 但是 where 能走到索引也可以的。

这样的操作虽然ok，但是会带来两个问题：

1. 翻页时重复/遗漏
2. 性能损耗

#### 避免重复/遗漏

想象一下，你有 5 4 3 2 1 五条记录。第一页你得到了【5，4】，如果你插入了一条数据 6，得到了 6 5 4 3 2 1，此时你再 limit 2, 2 实际上得到的是【4，3】，**重复了**。如果你删除了数据 4，得到5 3 2 1，此时再翻页，得到的是【2，1】，你**遗漏**了一条数据 3。

- 针对重复的问题，可以屏蔽后面插入的数据。具体来说，前端再多传一个 firstQueryTime，然后分页的时候只筛选出 where created_time < firstQueryTime 的数据。**相当于是照了一个快照。**
- 针对遗漏的问题，可以使用逻辑删除不太优雅地实现。也是使用 firstQueryTime，然后分页的时候筛选 where is_deleted = 0 or (is_deleted = 1 and updated_time > firstQueryTime) 的数据。

> 就算没有数据的插入/删除，limit 也可以发生重复/遗漏。
> 
> 这是因为我们前面提到，limit 是基于排序的，MySQL内置了多种排序算法，一些情况下如果使用了不稳定的排序算法（如堆排），就会出现排序不稳，limit发生重复/遗漏的情况。
> 
> 解决的办法可以用唯一字段，如id，作为第二排序字段，从而保证整个排序字段是唯一的。

#### 性能优化

如果只是翻个前几页，性能是还可以的。但是一旦翻的页数多了，例如 limit 10000, 10，此时就会暴露出严重的性能问题。为什么会这样？

我们看看一条语句：`select * from user where level > 3 order by id limit 10000, 10` (idx_level), 这条语句执行的过程是这样的：

1. 根据 idx_level， 捞出满足条件 level > 3 的所有 id（idx_level 是**非聚簇索引**，只能得到主键 id）
2. 是对于每个 id，都需要回表找到对应的整条记录。
3. limit 10000, 10 意味着至少要回表 10010 次（这个至少取决于有没有索引），然后丢掉前 10000 条记录，选出最后 10 条返回。

可以发现，性能的痛点在于「回表」。我们真正只需要回表 10 次，但是 limit 10000, 10 却做了 10010 次无意义的回表。

回表是面向索引和id的，我们可以从这方面入手，我只取排序字段（此处是id）不回表，拿到 id 后，从 id 往后查 10 个，不就好了吗？也就是写成这样：

`select * from user where id >= (select id from user where level > 3 order by id limit 10000, 1) order by id limit 10`

分析一下，发现内层子查询是不回表的，因为索引当中存了 (level, id), 只在外层回表了10次。相比于原始的方法有大大的提升。

**避免回表的本质是把非聚簇索引转移到聚簇索引（主键id）的索引上。**

基于以上思想，对于 limit M, N 我们可以提出几个改进策略：

1. 内层子查询：在内层嵌一个子查询，查出起始的 order by 字段（id） limit M, 1，然后在外层直接 字段 > 某值 limit N。
> 用 order by age 也是类似的：
> 
> `select * from user where age >= (select age from user where level > 3 order by age limit 10000, 1) order by age limit 10`。 
> 
> 如果有idx_level_age, 内层是不需要回表的。
2. INNER JOIN：先在临时表里面查出 id，再用通过 INNER JOIN 回表。
> 还是上面的例子，可以改造为：
> 
> `select * from user t, (select id from user where level > 3 order by age limit 10000, 10) sub_t where t.id = sub_t.id order by age`
> 
> 同样的，只要有 idx_level_age, 由于索引覆盖，也是不需要回表的。
3. 从业务层中带入上一页的字段，在本轮当中直接带上 字段 > 值即可。称之为标签记录法。

以上的三种策略中，方法3与业务耦合，不建议采用。

方法1和方法2类似，只要保证内层有索引覆盖不会回表就行。但是！一般建议用方法2，看下面两个语句：

> `select * from user where age >= (select age from user where level > 3 order by age limit 10000, 1) order by age limit 10`
> 
> `select * from user t, (select id from user where level > 3 order by age limit 10000, 10) sub_t where t.id = sub_t.id order by age`

如果 user 表中**不存在 age 的索引**，那么最外边的 order by 是要全表扫描的。而区别就在这里：

- 对于方法1，全表扫描的范围是**所有 age > xxx 的数据**，量很大。
- 对于方法2，全表扫描的范围是 **JOIN 后的数据集合**，有多大呢？在这里只有10条。

因此，更建议采用方法2。

另外，如果 order by 字段和 id 是趋势一致的（如自增 id + created_time），那么可以用 id 来 order by。

### Redis 实现

在 Redis 中实现首先考虑**如何存储数据**，由于分页的场景通常与排序有关，因此可以考虑用 zset 实现。在 zset 中，整条数据（或者 id）作为value，然后排序字段作为 score。

Redis 中提供了一个很方便的接口：`ZREVRANGEBYSCORE key Max Min LIMIT offset count`，表示选取 max -> min 之间 max 偏移 offset 个后降序 count 个的结果。

通常来说我们不关注 min 的取值，只需要传入 max, offset, count 即可。

在初始的时候，max = INF, offset = 0。第一页翻完后，需要计算新的 max 和 offset。新的 max 为本页的最小 score，新的 offset 为本页最小 score 的个数。

> 为什么需要 offset 呢？
> 
> 因为 score 可能相同。例如 score 是 6 5(1) 5(2) 4 3 的5条数据，如果没有 offset 的话，【6，5(1)】后翻下一页会变成【5(1), 5(2)】造成重复。

以 【6 5 5 5 4 3】，ps = 2 为例。

- 翻第一页时，max = INF, offset = 0，翻出【6 5】。max = min(score) = 5, offset = count(score = 5) = 1.
- 翻第二页时，max = 5, offset = 1, 翻出【5 5】。max = min(score) = 5, offset = count(score = 5) = 2.
- 翻第三页时，max = 5, offset = 2, 翻出【4 3】。max = min(score) = 3, offset = count(score = 3) = 1.

可以看到，Redis 的实现方法类似于 MySQL 的标签记录法。

不过由于 Redis 的 zset 本身是 score 有序的，结构更简单，实现起来要比 MySQL 简单许多。

！！通过双 Token + 非对称加密验证机制以及salt值实现便捷、安全的单点登录
！！通过自定义注解 + AOP 实现了可扩展的用户权限管控体系，可以实现接口粒度的精细权限控制
！！针对粉丝列表的深分页性能问题，基于内联结优化方法手写 MyBatis 插件 DeepPagerInterceptor 进行性能优化。在百万分页的情况下，优化性能达81.23%。
！！基于推拉模式 + 消息队列 + Redis ZSet 实现UP主视频动态异步推送、滚动分页、异步点赞等。
！！缓存
！！dfs


login时怎么加密密码:

> raw -enc-> web
>
> raw -md5-> db

UserSupport -- ServletRequestAttributes！--- ContextHolder中用ThreadLocal存放了对应的request，获取即可。


@autowired & @resource diff？

register: user, userInfo, userCoin

游客怎么认定权限！ --- 需要用户的时候会搞一个getCurrentUserId，确认身份游客没有token会抛异常！

关注模型：

follower -M-M- following
|--> 关注关系
关注关系 -M-1- followingGroup

怎么查看是否互相关注呢？--- 在添加关注的时候，check 是否存在另一个反向的关注关系。