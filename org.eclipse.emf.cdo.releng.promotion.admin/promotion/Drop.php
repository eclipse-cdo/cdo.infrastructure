<?php

require_once 'Project.php';

class Drop
{
	public $project;
	public $qualifier;
	public $path;
	public $visible;
	public $staging;
	public $label = "";
	public $stream = "";
	public $train = "";

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;

		$this->path = $this->project->path . "/" . $this->qualifier;
		$this->visible = !is_file($this->path . "/.invisible");

		$file = $this->path . "/web.properties";
		if (is_file($file))
		{
			$contents = file_get_contents($file);
			if (preg_match('@web\.label=(.*)$@s', $contents, $match))
			{
				$this->label = $match[1];
			}
		}

		$file = $this->path . "/build-info.xml";
		if (is_file($file))
		{
			$contents = file_get_contents($file);
			if (preg_match('@stream="(.*?)"@s', $contents, $match))
			{
				$this->stream = $match[1];
			}

			if (preg_match('@train="(.*?)"@s', $contents, $match))
			{
				$this->train = $match[1];
			}
		}
	}

	function generate()
	{
		echo '<tr>';
		$this->td($this->stream);
		$this->td($this->qualifier, $this->visible);
		$this->td($this->label, $this->visible);
		$this->td($this->train);
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
}

?>
