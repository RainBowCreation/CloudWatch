# CloudWatch
 [AWS CloudWatch](https://aws.amazon.com/cloudwatch/) monitoring plugin for Minecraft.

Support 1.20.1+ java 17+ | Multipaper supported!

 Requires the [Systems Manager](https://aws.amazon.com/systems-manager/) agent to have a role with the `cloudwatch:PutMetricData` [permission](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/permissions-reference-cw.html)
 
 This plugin is designed for Unix Operating Systems, such as Linux, and some statistics may not be recorded on other operating systems.

 ## Java Statistics
 All Java statistics collected are per minute and represent the current value or the count/total time during that period.

- Number of Garbage Collections
- Time spent performing Garbage Collection
- Heap Size
- Heap Max Size
- Heap Free Size
- Heap Used Size
- Number of Threads
- Number of Open File Descriptors
- Maximum File Descriptors
- Total Physical Memory Size
- Free Physical Memory Size
- Used Physical Memory Size
- Process CPU Load
- System CPU Load

 ## Minecraft Statistics
 All Minecraft statistics collected are per minute and represent the maximum value, count or total time during that period.

- Number of Online Players
- Maximum Tick Time
- Ticks per Second
- Number of Chunks Loaded
- Number of Chunks Populated
- Number of Creatures Spawned
- Number of Entity Deaths
- Number of Inventories Closed
- Number of Inventories Opened
- Number of Inventory Clicks
- Number of Inventory Drags
- Number of Items Despawned
- Number of Items Spawned
- Number of Items Players Dropped
- Number of Player Experience Changes
- Number of Player Interactions
- Number of Projectiles Launched
- Number of Structures Grown
- Number of Trades Selected

## Autoscaling support: Auto draining

``System manager`` will need an inline policy for automatic draining player support
 ```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ssm:GetParameter",
                "ssm:DeleteParameter"
            ],
            "Resource": "arn:aws:ssm:*:*:parameter/minecraft/drain/*"
        }
    ]
}
```
for draining player fire a lampda fuction that put parameter ``/minecraft/drain/<instance_id>`` to ``true`` and server will start draining people (kick) and automatically shutdown (minecraft server)
```
name = f"/minecraft/drain/{instance_id.strip()}"
ssm.put_parameter(Name=name, Value=value_str, Type="String", Overwrite=True)
```
