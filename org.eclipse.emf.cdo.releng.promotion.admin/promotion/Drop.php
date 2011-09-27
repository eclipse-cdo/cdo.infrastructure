<?php

require_once 'Project.php';

class Drop
{
	public $project;
	public $qualifier;
	public $type;
	public $path;
	public $visible;
	public $staged;
	public $label = "";
	public $stream = "";
	public $train = "";

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;
		$this->type = substr($qualifier, 0, 1);

		$this->path = $this->project->path . "/" . $this->qualifier;
		$this->visible = !is_file($this->path . "/.invisible");
		$this->staged = is_file($this->path . "/.staged");

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
		$href = 'http://www.eclipse.org/cdo/downloads/#'.str_replace('-', '_', $this->qualifier);

		echo '<tr>';
		$this->td('<a href="'.$href.'">'.$this->qualifier.'</a>');
		$this->td($this->label);
		$this->td('<a href="?Label='.$this->qualifier.'" title="Change label"><img src="images/edit.png"></a>');

		$intrain = in_array($this->train, $this->project->trains);
		if ($intrain && $this->staged)
		{
			$this->td('<a href="https://hudson.eclipse.org/hudson/job/'.$this->train.'.runAggregator">'.ucfirst($this->train).'</a>');
		}
		else
		{

			$this->td(ucfirst($this->train));
		}

		if ($this->staged || !$intrain)
		{
			$this->td();
		}
		else
		{
			$this->td('<a href="?Stage='.$this->qualifier.'" title="Stage"><img src="images/stage.png"></a>');
		}

		$visibility = $this->visible ? "Hide" : "Show";
		$this->td('<a href="?'.$visibility.'='.$this->qualifier.'" title="'.$visibility.'"><img src="images/'.$visibility.'.png"></a>');

		if ($this->staged || $this->type == "R")
		{
			$this->td();
		}
		else
		{
			$this->td('<a href="?Delete='.$this->qualifier.'" title="Delete"><img src="images/delete.gif"></a>');
		}

		echo '</tr>';
	}

	function stage()
	{
		echo $this->qualifier." staged";
	}

	function hide()
	{
		echo $this->qualifier." invisible";
	}

	function show()
	{
		echo $this->qualifier." visible";
	}

	function delete()
	{
		echo $this->qualifier." deleted";
	}

	private function td($str = "&nbsp;")
	{
		if ($str == "")
		{
			$str = "&nbsp;";
		}

		echo '<td align="center" bgcolor="'.($this->staged ? "#EEEEEE" : "#FFFFFF").'">';
		if ($this->staged) echo "<b>";
		if (!$this->visible) echo "<i>";
		echo $str;
		if (!$this->visible) echo "</i>";
		if ($this->staged) echo "</b>";
		echo '</td>';
	}
}

?>
