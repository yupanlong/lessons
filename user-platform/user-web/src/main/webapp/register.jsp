<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf"/>
    <title>User Register</title>
    <title>用户注册</title>
</head>
<body>
<div class="container">
    <form class="form-signin" action="${pageContext.request.contextPath}register" method="post">
        <h1 class="h3 mb-3 font-weight-normal">注册</h1>
        <label for="inputEmail" class="sr-only">请输入电子邮件</label>
        <input  type="email" id="inputEmail" name="email" class="form-control" placeholder="请输入电子邮件" required autofocus>
        <label for="inputUserName" class="sr-only">用户名</label>
        <input type="text" id="inputUserName" name="userName" class="form-control" placeholder="请输入用户名" required>
        <label for="inputPassword" class="sr-only">密码</label>
        <input type="password" id="inputPassword" name="password" class="form-control" placeholder="请输入密码" required>
        <label for="inputPhone" class="sr-only">手机号</label>
        <input type="phone" id="inputPhone" name="phone" class="form-control" placeholder="请输入手机号" required>
        <button class="btn btn-lg btn-primary btn-block"  type="submit">register</button>
        <p class="mt-5 mb-3 text-muted">&copy; 2017-2021</p>
    </form>
</div>
</body>
