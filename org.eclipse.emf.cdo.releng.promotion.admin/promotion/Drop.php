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
	public $job = "";
	public $number = "";

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

			if (preg_match('@job="(.*?)"@s', $contents, $match))
			{
				$this->job = $match[1];
			}

			if (preg_match('@number="(.*?)"@s', $contents, $match))
			{
				$this->number = $match[1];
			}
		}
	}

	function generate()
	{
		$href = 'http://www.eclipse.org/cdo/downloads/#'.str_replace('-', '_', $this->qualifier);

		echo '<tr>';

		if (is_dir("/shared/jobs/emf-cdo-integration/builds/".$this->number))
		{
			$this->td('<a href="https://hudson.eclipse.org/hudson/job/emf-cdo-integration/'.$this->number.'" title="Jump to Hudson build">'.$this->number.'</a>');
		}
		else
		{
			$this->td($this->number);
		}

		$this->td('<a href="'.$href.'" title="Jump to downloads page" target="downloads">'.$this->qualifier.'</a>');
		$this->td($this->label);
		$this->td('<a href="?action=EditLabel&drop='.$this->qualifier.'" title="Change drop label"><img src="images/edit.gif"></a>');

		$visibility = $this->visible ? "Hide" : "Show";
		$this->td('<a href="?action='.$visibility.'&drop='.$this->qualifier.'" title="'.$visibility.' this drop"><img src="images/'.$visibility.'.png"></a>');

		$this->td('<a href="?action=AskDuplicate&drop='.$this->qualifier.'" title="Duplicate this drop"><img src="images/copy.gif"></a>');

		$intrain = in_array($this->train, $this->project->trains);
		if ($intrain && $this->staged)
		{
			$this->td('<a href="https://hudson.eclipse.org/hudson/job/'.$this->train.'.runAggregator" title="Jump to aggregator page" target="_blank">'.ucfirst($this->train).'</a>');
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
			$old = $this->project->getStagedDrop($this->train);
			if ($old)
			{
				$old = $old->qualifier;
			}

			$this->td('<a href="?action=AskStage&drop='.$this->qualifier.'&train='.$this->train.'&old='.$old.'" title="Stage this drop"><img src="images/stage.png"></a>');
		}

		if ($this->staged || $this->type == "R")
		{
			$this->td();
		}
		else
		{
			$this->td('<a href="?action=AskDelete&drop='.$this->qualifier.'" title="Delete this drop"><img src="images/delete.gif"></a>');
		}

		echo '</tr>';
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
