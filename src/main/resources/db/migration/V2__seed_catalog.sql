insert into lesson (id, track, position, title, objective, body, example) values
('html-css', 'FRONTEND', 1, 'HTML 与 CSS', '用语义化 HTML 搭出页面，并让布局适应不同屏幕。',
 'HTML 描述内容的结构，CSS 决定它如何呈现。用 main、section 和标题元素表达层级；交互优先用原生 button。布局从窄屏开始，根据空间用 Grid 扩展为多列。盒模型的 padding 位于内容与边框之间，margin 在边框之外。',
 '<section aria-labelledby="route-title">
  <h2 id="route-title">学习路线</h2>
  <button type="button">展开详情</button>
</section>

.routes { display: grid; gap: 1rem; }
@media (min-width: 48rem) {
  .routes { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}'),
('dom-events', 'FRONTEND', 2, 'DOM 与浏览器事件', '用 DOM 查询、事件监听和文本更新实现交互。',
 'DOM 把页面暴露成节点树。先查询目标元素，再用 addEventListener 监听事件。只需要展示字符串时，用 textContent 更新文字。监听表单的 submit 事件还可以覆盖键盘提交，不必只监听按钮点击。',
 'const form = document.querySelector("#lesson-form");
const output = document.querySelector("#preview");
form.addEventListener("submit", (event) => {
  event.preventDefault();
  output.textContent = new FormData(form).get("title");
});'),
('java-core', 'JAVA', 3, 'Java 基础', '理解对象相等、集合选择与资源关闭。',
 '对象相等由 equals 定义；相等的对象必须有相同的 hashCode。HashMap 不提供线程安全保证，多线程共享更新时可考虑 ConcurrentHashMap。实现 AutoCloseable 的资源可以交给 try-with-resources，在离开代码块时关闭。',
 'record LessonKey(String id) {}

var cache = new java.util.concurrent.ConcurrentHashMap<LessonKey, String>();
cache.put(new LessonKey("java-core"), "Java 基础");

try (var reader = java.nio.file.Files.newBufferedReader(path)) {
    return reader.readLine();
}'),
('spring-backend', 'JAVA', 4, 'Spring MVC 与 JPA', '区分页面和 JSON 响应，并理解共享 Bean 与事务。',
 'Spring MVC 负责路由与响应；普通控制器可以返回模板名，RestController 的返回值写入响应体。业务服务通常是单例，共享同一 Bean 实例的多次请求不应把请求状态放进实例字段。让 Service 负责事务边界，由 Repository 持久化数据；默认情况下未处理的运行时异常与 Error 会触发回滚。',
 '@Service
class PracticeService {
    private final AttemptRepository attempts;

    PracticeService(AttemptRepository attempts) {
        this.attempts = attempts;
    }

    @Transactional
    void submit(Attempt attempt) {
        attempts.save(attempt);
    }
}'),
('http-json', 'INTEGRATION', 5, 'HTTP 与 JSON', '读懂浏览器和服务器交换的请求、状态码与响应体。',
 'HTTP 请求包含方法和目标地址。成功读取资源通常返回 200，资源不存在返回 404。JSON 是交换数据的格式，不是 HTML 模板。调试时在 Network 面板查看方法、请求路径、状态码和响应体。',
 'GET /api/lessons/java-core

HTTP/1.1 200 OK
Content-Type: application/json

{"title":"Java 基础","objective":"理解对象相等、集合选择与资源关闭。","completed":false}'),
('fetch-demo', 'INTEGRATION', 6, '用 fetch 完成一次联调', '区分 HTTP 错误与网络异常，并在界面反馈结果。',
 '动手试试下方两个按钮，在浏览器开发者工具的 Network 面板分别找到 200 和 404。fetch 收到 HTTP 404 时通常仍返回 Response；要检查 response.ok，再读取 JSON。网络连接失败则会拒绝 Promise。',
 'const response = await fetch("/api/lessons/java-core");
if (!response.ok) {
  throw new Error(`HTTP ${response.status}`);
}
const lesson = await response.json();
console.log(lesson.title);');

insert into question (id, lesson_id, prompt, option_a, option_b, option_c, correct_option, explanation) values
(1, 'html-css', '要用键盘激活“查看解析”控件，哪种 HTML 最适合作为起点？',
 '<div>查看解析</div>', '<button type="button">查看解析</button>', '<span>查看解析</span>', 'B',
 '原生 button 自带焦点与键盘激活语义；div 或 span 不会自动获得这些行为。'),
(2, 'html-css', '卡片文字离边框太近，想增加卡片内部留白，应改哪个属性？',
 'padding', 'margin', 'outline', 'A', 'padding 位于内容与边框之间。margin 调整元素外侧距离，outline 不为内容增加留白。'),
(3, 'html-css', '窄屏显示一列、宽屏显示两列，哪种写法最合适？',
 '将所有卡片宽度固定为 400px', '用两个 br 插入换行', '默认一列，在媒体查询中切换 Grid 列数', 'C',
 '媒体查询根据视口改变布局，无需额外标记或固定卡片宽度。'),
(4, 'dom-events', '怎样在默认会提交的表单上先拦截提交，再显示本地预览？',
 '在表单的 submit 事件里调用 event.preventDefault()', '设置表单的 display: block', '在 submit 监听器里只调用 event.stopPropagation()', 'A',
 'preventDefault 取消默认提交；stopPropagation 只改变事件传播。'),
(5, 'dom-events', '用户输入一段含有尖括号的文字，要原样显示，不当成 HTML，应该怎么做？',
 '赋值给 innerHTML', '赋值给 textContent', '把字符串传给 insertAdjacentHTML', 'B',
 'textContent 设置纯文本；另外两个选项会把字符串交给 HTML 解析器。'),
(6, 'dom-events', '既要处理点提交按钮，也要处理在输入框按 Enter 触发的表单提交，监听什么？',
 '输入框的 mouseover', '按钮的 focus', '表单的 submit', 'C',
 '监听表单的 submit 能统一处理不同触发方式。'),
(13, 'http-json', '浏览器请求一个不存在的课程，服务端应使用什么状态码？',
 '200', '404', '201', 'B', '404 表示资源不存在；200 表示成功，201 表示创建成功。'),
(14, 'http-json', '只读课程摘要且不改变学习进度，最合适的 HTTP 方法是？',
 'POST', 'DELETE', 'GET', 'C', 'GET 用于读取资源，不应推进课程状态。'),
(15, 'http-json', 'Network 面板显示状态 200，但前端收到 HTML。下一步先检查什么？',
 '响应 Content-Type、实际响应体与请求 URL', 'Java 主类的文件名', '显示器的分辨率', 'A',
 '200 只说明请求成功。检查 URL 和响应头、响应体，可判断是否访问了 HTML 页面而非 JSON 接口。'),
(16, 'fetch-demo', 'fetch 已收到服务器返回的 404。怎样在读 JSON 前识别失败？',
 '只写 catch 就够了', '检查 response.ok', '判断请求 URL 是否包含问号', 'B',
 'HTTP 错误通常仍返回 Response；response.ok 仅在 2xx 时为 true。'),
(17, 'fetch-demo', 'fetch 因网络中断拒绝 Promise，怎样向用户显示可读错误？',
 '用 try/catch 捕获并更新状态提示', '把代码改成 response.status === 200', '忽略错误、一直显示加载中', 'A',
 '网络异常时没有可供检查状态码的 Response；需要捕获拒绝并结束加载状态。'),
(18, 'fetch-demo', '联调刚失败，要确认请求路径和状态码，应优先打开哪个面板？',
 'Elements', 'Sources', 'Network', 'C',
 'Network 记录路径、方法、状态码和响应体，适合追踪浏览器到服务器的一次往返。');

insert into question values
(7, 'java-core', '用课程编号定义 LessonKey.equals。若两个键的 equals 返回 true，它们还必须满足什么？',
 '两个对象必须是同一实例', 'hashCode 返回值必须相同', 'toString 必须相同', 'B',
 'Java Object 契约要求相等对象具备相同的哈希值，反过来不成立。用作 Map 键时，按同一组用于相等判断的字段实现 hashCode。',
 'CANDIDATE_ACCOUNT', '牛客 · 淘天一面复盘（候选人自述）', 'https://www.nowcoder.com/discuss/734830070774104064', 'https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html#hashCode()'),
(8, 'java-core', '多个请求会更新同一张课程访问计数表。哪种写法能对单个键安全地累加？',
 '使用 ConcurrentHashMap 实例 counts，并调用 counts.merge(key, 1, Integer::sum)', '使用 HashMap 实例 counts，调用 counts.merge(key, 1, Integer::sum)，不做同步', '在 ConcurrentHashMap 实例上分开执行 get 和 put', 'A',
 'ConcurrentHashMap 的 merge 对该键原子执行。HashMap 未提供线程安全保证；分开的 get 和 put 也可能丢失更新。',
 'CANDIDATE_ACCOUNT', '牛客 · 字节跳动后台开发一面（候选人自述）', 'https://www.nowcoder.com/discuss/353156168182865920', 'https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html'),
(9, 'java-core', 'try (var reader = Files.newBufferedReader(path)) 中，读取途中抛出异常。已打开的 reader 会怎样？',
 '只有正常返回才会关闭', '只能等垃圾回收时关闭', '离开 try 时会尝试自动关闭', 'C',
 '资源初始化成功后，无论代码块正常完成还是抛异常，try-with-resources 都会调用 close；多个资源按声明的逆序关闭。',
 'COMMUNITY', 'JavaGuide · Java 基础面试考点', 'https://github.com/Snailclimb/JavaGuide-Interview/blob/master/docs/java/java-basis.md', 'https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.20.3'),
(10, 'spring-backend', '默认单例的练习服务把当前请求选择的选项保存在实例字段中。怎样避免并发提交互相覆盖？',
 '把字段改为 public', '让选项作为方法参数或局部变量传递', '把字段改为 static', 'B',
 '默认单例 Bean 的同一个实例会服务多个请求。将请求独有的数据保留在方法参数或局部变量内，不在实例字段中共享。',
 'CANDIDATE_ACCOUNT', '牛客 · 腾讯校招 Java 后端面经（候选人自述）', 'https://www.nowcoder.com/feed/main/detail/8682e4ae96114b588995ecad345bf31c', 'https://docs.spring.io/spring-framework/reference/7.0/core/beans/factory-scopes.html'),
(11, 'spring-backend', '一个 Controller 返回模板名，另一个专门提供课程 JSON。哪个注解默认把处理方法的返回值写入响应体？',
 '@RestController', '@Controller', '@Service', 'A',
 'RestController 组合了 Controller 与 ResponseBody。普通 Controller 可以返回模板名，也可在特定方法上加 ResponseBody。',
 'CANDIDATE_ACCOUNT', '牛客 · 小红书一面复盘（候选人自述）', 'https://www.nowcoder.com/feed/main/detail/c701af98e9cc4edd9024fa563e5fa6ee', 'https://docs.spring.io/spring-framework/reference/7.0/web/webmvc/mvc-controller/ann-methods/responsebody.html'),
(12, 'spring-backend', '事务已生效，沿用 Spring 默认回滚规则。保存作答后，哪种未被捕获的异常默认会使事务回滚？',
 'IOException', 'SQLException', 'IllegalStateException', 'C',
 'IllegalStateException 属于 RuntimeException。默认回滚未处理的 RuntimeException 与 Error；受检异常不会仅因抛出而默认回滚，可显式配置 rollbackFor。',
 'COMMUNITY', 'JavaGuide · Spring 与 Spring Boot 面试考点', 'https://github.com/Snailclimb/JavaGuide-Interview/blob/master/docs/system-design/spring.md', 'https://docs.spring.io/spring-framework/reference/7.0/data-access/transaction/declarative/rolling-back.html');
