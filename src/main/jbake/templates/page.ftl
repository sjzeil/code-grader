<#include "header.ftl">

	<div class="center">
	    <#include "menu.ftl">
        <div class="rightPart">
            <div class="page-header">
                <h1><#escape x as x?xml>${content.title}</#escape></h1>
            </div>
            <p><em>${content.date?string("dd MMMM yyyy")}</em></p>
			<p>${content.body}</p>
		</div>
	</div>
	<hr />

<#include "footer.ftl">