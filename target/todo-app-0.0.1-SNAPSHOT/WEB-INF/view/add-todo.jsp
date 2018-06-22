<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

    <div class="container">
        <h3>Add a new todo</h3>
        <form method="POST" action="/add-todo.do">
            <fieldset class="form-group">
                <label>Description:</label>
                <input name="newTodo" type="text" class="form-control"/><br>
            </fieldset>
            <fieldset class="form-group">
                <label>Category:</label>
                <input name="category" type="text" class="form-control" >
            </fieldset>
            <input class="btn btn-success" name="add" type="submit" value="Submit"/>
        </form>
    </div>

    <%@ include file="../common/footer.jspf"%>

</body>
</html>