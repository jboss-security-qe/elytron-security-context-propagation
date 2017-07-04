<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>Elytron Security context propagation</title>

    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <link href="https://maxcdn.bootstrapcdn.com/css/ie10-viewport-bug-workaround.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="starter-template.css" rel="stylesheet">
  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Project name</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="container">

      <div class="starter-template">
        <h1>Security context propagation test</h1>
        <p class="lead">Use this app to test behavior between servers.</p>
      </div>

<h2>Call the Entry bean</h2>
<c:set var="enumValues" value="<%=org.wildfly.test.seccontext.shared.ReAuthnType.values()%>"/>
      
<form class="form-horizontal" action="<c:url value='/CallEntryServlet'/>" method="get">
  <fieldset>
    <legend>Bean call configuration</legend>
    <div class="form-group">
      <label for="url" class="col-lg-2 control-label">Server1 URL</label>
      <div class="col-lg-10">
        <input type="text" class="form-control" id="url" placeholder="Url" name="url" value="remote+http://127.0.0.1:8080">
      </div>
    </div>
    <div class="form-group">
      <label for="method" class="col-lg-2 control-label">Entry bean method</label>
      <div class="col-lg-10">
        <select class="form-control" id="method" name="method">
          <option>whoAmI</option>
          <option>doubleWhoAmI</option>
          <option>doIHaveRole</option>
          <option>doubleDoIHaveRole</option>
        </select>
      </div>
    </div>
    <div class="form-group">
      <label for="role" class="col-lg-2 control-label">Role to check (for do*IHaveRole methods)</label>
      <div class="col-lg-10">
        <select class="form-control" id="role" name="role">
          <option>entry</option>
          <option>whoami</option>
          <option>servlet</option>
        </select>
      </div>
    </div>
  </fieldset>
  <fieldset>
  	<legend>Switch identity in servlet</legend>
    <div class="form-group">
      <label for="type" class="col-lg-2 control-label">Switch identity type</label>
      <div class="col-lg-10">
        <select class="form-control" id="type" name="type">
        <c:forEach items="${enumValues}" var="enumValue">
        	<option>${enumValue}</option>
		</c:forEach>
        </select>
      </div>
    </div>
    <div class="form-group">
      <label for="username" class="col-lg-2 control-label">Entry bean username</label>
      <div class="col-lg-10">
        <input type="text" class="form-control" id="username" placeholder="Username" name="username">
      </div>
    </div>
    <div class="form-group">
      <label for="password" class="col-lg-2 control-label">Entry bean password</label>
      <div class="col-lg-10">
        <input type="password" class="form-control" id="password" placeholder="Password" name="password">
      </div>
    </div>
  </fieldset>
  <fieldset>
  	<legend>Switch identity in Entry bean (only valid for double* methods)</legend>
    <div class="form-group">
      <label for="type2" class="col-lg-2 control-label">Switch identity type</label>
      <div class="col-lg-10">
        <select class="form-control" id="type2" name="type2">
        <c:forEach items="${enumValues}" var="enumValue">
        	<option>${enumValue}</option>
		</c:forEach>
        </select>
      </div>
    </div>
    <div class="form-group">
      <label for="username2" class="col-lg-2 control-label">WhoAmI bean username</label>
      <div class="col-lg-10">
        <input type="text" class="form-control" id="username2" placeholder="Username" name="username2">
      </div>
    </div>
    <div class="form-group">
      <label for="password2" class="col-lg-2 control-label">WhoAmI bean password</label>
      <div class="col-lg-10">
        <input type="password" class="form-control" id="password2" placeholder="Password" name="password2">
      </div>
    </div>
  </fieldset>
    
    <div class="form-group">
      <div class="col-lg-10 col-lg-offset-2">
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </div>
</form>
    </div><!-- /.container -->


    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="https://maxcdn.bootstrapcdn.com/js/ie10-viewport-bug-workaround.js"></script>
  </body>
</html>
