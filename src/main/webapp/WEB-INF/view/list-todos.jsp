<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

    <div class="container">
        <h1>Welcome ${name}</h1>
        <h3>Todo List</h3>

        <table class="table table-striped">
            <caption></caption>
            <thead>
                <th>Description</th>
                <th>Category</th>
                <th>Actions</th>
            </thead>
            <tbody>
                <c:forEach items="${todos}" var="todo">
                <tr>
                    <td>${todo.name}</td>
                    <td>${todo.category}</td>
                    <td><a class="btn btn-danger" href="/delete-todo.do?todo=${todo.name}&category=${todo.category}">Delete</a></td>
                </tr>
                </c:forEach>
                <tr>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </tbody>
        </table>
        <p>
            <font color="red">${errorMessage}</font>
        </p>
        <a class="btn btn-success" href="/add-todo.do" class="navbar-brand">Create a new ToDo</a>
    </div>

    <%@ include file="../common/footer.jspf"%>

</body>
</html>