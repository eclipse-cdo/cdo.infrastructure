<?php

require_once 'Project.php';

class Drop
{
	private $project;
	private $qualifier;
	private $staging;

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;

	}

	function getProject()
	{
		return $this->project;
	}

	function getQualifier()
	{
		return $this->qualifier;
	}
}

?>
