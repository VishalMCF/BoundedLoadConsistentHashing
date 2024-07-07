import React, { useEffect, useState } from 'react';
import { Grid, CssBaseline, Box, Typography } from '@mui/material';
import SunburstChart from './SunburstChart';
import consistentData from './consistentData.json';
import LeftPanel from './LeftPanel';
import axios from 'axios';

const Index = () => {
    const [partitionCount, setPartitionCount] = useState(20);
    const [replicationFactor, setReplicationFactor] = useState(1);
    const [finalData, setFinalData] = useState(null)
    const [members, setMembers] = useState([
        { name: 'Node1' },
        { name: 'Node2' },
        { name: 'Node3' },
        { name: 'Node4' },
    ]);
    const [isPanelOpen, setIsPanelOpen] = useState(true);

    const handleDeleteMember = (index) => {
        setMembers(members.filter((_, i) => i !== index));
    };

    const handleAddMember = () => {
        setMembers([...members, { name: `Node${members.length + 1}` }]);
    };

    const handleChangeMember = (index, value) => {
        const newMembers = [...members];
        newMembers[index].name = value;
        setMembers(newMembers);
    };

    function convertData(data) {
        const { partitionMap } = data;
    
        // Create a map to collect children under each unique name
        const nameMap = {};
    
        for (const key in partitionMap) {
            const { name } = partitionMap[key];
            if (!nameMap[name]) {
                nameMap[name] = [];
            }
            nameMap[name].push({ name: key, value: 1 });
        }
    
        // Transform the map into the desired format
        const children = Object.keys(nameMap).map(name => ({
            name,
            children: nameMap[name]
        }));
    
        return {
            name: 'flare',
            children
        };
    }

    const handleSubmit = async () => {
        const payload = {
            "partitionCount": partitionCount,
            "replicationFactor": replicationFactor,
            "members": members
        }

        try {
            const response = await axios.post("http://localhost:8080/", payload)
            console.log(response.data)
            const filter = convertData(response.data)
            console.log(filter)
            setFinalData(filter)
        } catch (error) {
            console.log(error)
        }
    };

    useEffect(() => {
      handleSubmit()
    }, [])
    



    const togglePanel = () => {
        setIsPanelOpen(!isPanelOpen);
    };

    return (
        <div>
            <CssBaseline />
            <Grid container>
                {isPanelOpen && (
                    <Grid item xs={4}>
                        <LeftPanel
                            members={members}
                            onDeleteMember={handleDeleteMember}
                            onAddMember={handleAddMember}
                            onChangeMember={handleChangeMember}
                            partitionCount={partitionCount}
                            setPartitionCount={setPartitionCount}
                            replicationFactor={replicationFactor}
                            setReplicationFactor={setReplicationFactor}
                            onSubmit={handleSubmit}
                            isPanelOpen={isPanelOpen}
                            togglePanel={togglePanel}
                        />
                    </Grid>
                )}
                <Grid item xs={isPanelOpen ? 8 : 12}>
                    <Box p={2}>
                        <Typography variant="h4" gutterBottom>
                            Consistent Hashing Visualization
                        </Typography>
                        {finalData && <SunburstChart data={finalData} />}
                    </Box>
                </Grid>
            </Grid>
        </div>
    );
};

export default Index;
