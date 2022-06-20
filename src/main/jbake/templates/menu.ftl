<div class="leftPart">
  <div class="menuBlock">
    <span class="menuBlockHeader"><a href="<#if
										   (content.rootpath)??>${content.rootpath}<#else></#if>index.html">Home</a></span>
  </div>
  <div class="menuBlock">
    <span class="menuBlockHeader">Project Info</span>
	<ul>
      <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>javadoc.html">API (Javadoc)</a></li>
      <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>dependencies.html">Dependencies</a></li>
	</ul>
  </div>
  <div class="menuBlock">
    <span class="menuBlockHeader">Testing</span>
	<ul>
      <li><a href="<#if
      (content.rootpath)??>${content.rootpath}<#else></#if>junit.html">Unit
		  Tests</a></li>
	</ul>
  </div>
  <div class="menuBlock">
 	<span class="menuBlockHeader">Analysis Reports</span>
	<ul>
      <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>checkstyle.html">Checkstyle</a></li>
      <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>pmd.html">PMD</a></li>
	</ul>
  </div>
</div>
