<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

    <div class="container">
        My First JSP<br>
        ${success}
        <form action="/login.do" method="post">
            Enter your name <input type="text" name="name"/><br>
            Enter your password <input type="password" name="password" value="Login"l/><br>
            <input type="submit">
        </form>
    </div>

    <%@ include file="../common/footer.jspf"%>

</body>
</html>

