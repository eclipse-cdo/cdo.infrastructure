<?php

require_once 'Project.php';

class Drop
{
	private $project;
	private $qualifier;
	private $path;
	private $visible;
	private $staging;
	private $label = "";

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

	function getLabel()
	{
		return $this->label;
	}

	function generate()
	{
		echo '<tr>';
		$this->td('<font face="Courier">'.$this->qualifier.'</font>', $this->visible);
		$this->td($this->label);
		$this->td($this->visible ? "" : "invisible");
		echo '</tr>';
	}

	private function td($str = "&nbsp;", $visible = true)
	{
		if ($str == "")
		{
			$str = "&nbsp;";
		}

		echo '<td>';
		echo '<font color="'.($visible ? "#000000" : "#BBBBBB").'">';
		echo $str;
		echo '</font>';
		echo '</td>';
	}

	private function init()
	{
		$this->path = $this->getProject()->getPath() . "/" . $this->qualifier;
		$this->visible = !is_file($this->path . "/.invisible");

		$webprops = $this->path . "/web.properties";
		if (is_file($webprops))
		{
			// 			$this->label = file_get_contents($webprops);
			if (preg_match("@web\.label=(.*)@s", file_get_contents($webprops), $match))
			{
				echo "X";
				$this->label = $match[1];
			}
		}
	}
}

?>
