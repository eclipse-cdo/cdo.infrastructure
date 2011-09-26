<?php

require_once 'Project.php';

class Drop
{
	private $project;
	private $qualifier;
	private $path;
	private $visible;
	private $staging;

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;
		$this->init();
	}

	function getProject()
	{
		return $this->project;
	}

	function getQualifier()
	{
		return $this->qualifier;
	}

	function getPath()
	{
		return $this->path;
	}

	function isVisible()
	{
		return $this->visible;
	}

	function getStaging()
	{
		return $this->staging;
	}

	function generate()
	{
		echo '<tr>';
		$this->td($this->qualifier);
		$this->td($this->visible ? "visible" : "invisible");
		echo '</tr>';
	}

	private function td($str = "&nbsp;")
	{
		echo '<td>';
		echo '<font face="Courier"';
		if (!$this->visible)
		{
			echo ' color="#AAAAAA"';
		}

		echo '>';
		echo str;
		echo '</font>';
		echo '</td>';
	}
	private function init()
	{
		$this->path = $this->getProject()->getPath() . "/" . $this->qualifier;
		$this->visible = !is_file($this->path . "/.invisible");
	}
}

?>
